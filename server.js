const express = require("express");
const mysql = require("mysql2");
const cors = require("cors");
const path = require("path");
const admin = require("firebase-admin");

// Initialize Firebase Admin
const serviceAccount = require("./app/mapboost-d5b1c-firebase-adminsdk-fbsvc-91646d2dfc.json");
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const app = express();
app.use(cors());
app.use(express.json());

// Sambungan ke Database MySQL
const db = mysql.createConnection({
  host: "localhost",
  user: "root",
  password: "",
  database: "notifications_db"
});

db.connect((err) => {
  if (err) {
    console.log("❌ DB connection failed:", err);
  } else {
    console.log("✅ Connected to MySQL!");
  }
});

// 🌐 ROUTE FRONTEND: Serve fail statik dari folder 'public'
app.use(express.static(path.join(__dirname, 'public')));

app.get('/admin', (req, res) => {
  res.sendFile(path.join(__dirname, 'public', 'login.html'));
});

// 🌐 API ROUTES FOR ADMIN DASHBOARD
// A. Ambil Shop Locations
app.get('/api/admin/shops', (req, res) => {
  db.query('SELECT name, address, latitude, longitude FROM shops', (err, results) => {
    if (err) return res.status(500).json({ error: err.message });
    res.json(results);
  });
});

// B. Ambil Promotions
app.get('/api/admin/promotions', (req, res) => {
  const query = `
    SELECT s.name AS shop_name, p.title, p.discount 
    FROM promotions p
    JOIN shops s ON p.shop_id = s.id
  `;
  db.query(query, (err, results) => {
    if (err) return res.status(500).json({ error: err.message });
    const formatted = results.map(promo => ({
      display_text: `${promo.shop_name} - ${promo.title} (${promo.discount})`
    }));
    res.json(formatted);
  });
});

// C. Ambil Latest User Locations (VERSION TERKUAT - TRIM & LOWER)
app.get('/api/admin/user-locations', (req, res) => {
  const query = `
    SELECT u.username AS user, ul.latitude, ul.longitude, ul.recorded_at AS time
    FROM user_locations ul
    INNER JOIN users u ON TRIM(LOWER(ul.user_id)) = TRIM(LOWER(u.username))
    INNER JOIN (
        SELECT user_id, MAX(recorded_at) as max_time
        FROM user_locations
        GROUP BY user_id
    ) latest ON ul.user_id = latest.user_id AND ul.recorded_at = latest.max_time
    ORDER BY ul.recorded_at DESC
  `;

  db.query(query, (err, results) => {
    if (err) return res.status(500).json({ error: err.message });
    res.json(results);
  });
});

// D. Borang Send Notification
app.post('/api/admin/send-notification', (req, res) => {
  const { email, title, message } = req.body;
  if (!email || !title || !message) return res.status(400).json({ error: 'Please fill in all fields!' });

  db.query('SELECT fcm_token FROM users WHERE email = ?', [email], (err, results) => {
    if (err) return res.status(500).json({ error: err.message });
    if (results.length === 0) return res.status(404).json({ error: 'User not found' });

    const token = results[0].fcm_token;
    if (!token) {
      return res.status(400).json({ error: 'User does not have an FCM token registered!' });
    }

    console.log(`[FCM] Sending push notification to ${email} (Token: ${token})`);

    const payload = {
      notification: {
        title: title,
        body: message
      },
      token: token
    };

    admin.messaging().send(payload)
      .then((response) => {
        console.log('✅ Successfully sent FCM message:', response);
        res.json({ success: true, message: `Notification successfully sent to ${email}` });
      })
      .catch((error) => {
        console.error('❌ Error sending FCM message:', error);
        res.status(500).json({ error: `FCM Error: ${error.message}` });
      });
  });
});

// E. User Signup from Mobile App
app.post('/signup', (req, res) => {
  const { username, email, fcm_token } = req.body;
  if (!username || !email) {
    return res.status(400).json({ error: 'Username and email are required!' });
  }

  db.query('SELECT user_id FROM users WHERE username = ?', [username], (err, results) => {
    if (err) return res.status(500).json({ error: err.message });

    if (results.length > 0) {
      db.query('UPDATE users SET email = ?, fcm_token = ? WHERE username = ?', [email, fcm_token || '', username], (err) => {
        if (err) return res.status(500).json({ error: err.message });
        return res.json({ success: true, message: 'User details updated' });
      });
    } else {
      db.query('INSERT INTO users (username, email, fcm_token) VALUES (?, ?, ?)', [username, email, fcm_token || ''], (err) => {
        if (err) return res.status(500).json({ error: err.message });
        return res.status(201).json({ success: true, message: 'User registered' });
      });
    }
  });
});

// F. User Location Update from Mobile App
// app.post('/api/location', (req, res) => {
//   const { userId, lat, lon } = req.body;
//   console.log("DEBUG: Received data:", { userId, lat, lon }); // Track incoming data

//   db.query('INSERT INTO user_locations (user_id, latitude, longitude) VALUES (?, ?, ?)', [userId, lat, lon], (err) => {
//     if (err) {
//       console.error("❌ SQL ERROR:", err.message); // This will print the specific SQL failure reason
//       return res.status(500).json({ error: err.message });
//     }
//     res.json({ success: true, message: 'Location recorded' });
//   });
// });

// app.post('/api/location', (req, res) => {
//   const { userId, lat, lon } = req.body;
//   if (!userId || lat === undefined || lon === undefined) {
//     return res.status(400).json({ error: 'userId, lat, and lon are required!' });
//   }

//   // Query 1: Insert into user_locations
//   db.query('INSERT INTO user_locations (user_id, latitude, longitude) VALUES (?, ?, ?)', [userId, lat, lon], (err) => {
//     if (err) return res.status(500).json({ error: err.message });

//     // Query 2: Insert into check_ins immediately after
//     db.query('INSERT INTO check_ins (user_id, latitude, longitude) VALUES (?, ?, ?)', [userId, lat, lon], (err2) => {
//       if (err2) return res.status(500).json({ error: err2.message });

//       res.json({ success: true, message: 'Location recorded in both tables' });
//     });
//   });
// });

app.post('/api/location', (req, res) => {
  const username = req.body.userId;
  const lat = parseFloat(req.body.lat);
  const lon = parseFloat(req.body.lon);

  if (!username || isNaN(lat) || isNaN(lon)) {
    return res.status(400).json({ error: 'Valid userId (username), lat, and lon are required!' });
  }

  // 1. Look up the numeric user_id from users table using username
  db.query('SELECT user_id FROM users WHERE username = ?', [username], (err, results) => {
    if (err) return res.status(500).json({ error: "Database lookup error: " + err.message });
    
    let numericUserId = null;
    if (results.length > 0) {
      numericUserId = results[0].user_id;
    } else {
      // Fallback: check if the username itself is a numeric ID
      const parsedId = parseInt(username);
      if (!isNaN(parsedId)) {
        numericUserId = parsedId;
      }
    }

    if (!numericUserId) {
      return res.status(404).json({ error: `User '${username}' not found in database.` });
    }

    // 2. Insert into user_locations (stores username as string)
    db.query('INSERT INTO user_locations (user_id, latitude, longitude) VALUES (?, ?, ?)', [username, lat, lon], (err1) => {
      if (err1) return res.status(500).json({ error: "Location table error: " + err1.message });

      // 3. Insert into check_ins (stores numeric user_id as int)
      db.query('INSERT INTO check_ins (user_id, latitude, longitude) VALUES (?, ?, ?)', [numericUserId, lat, lon], (err2) => {
        if (err2) return res.status(500).json({ error: "Check-in table error: " + err2.message });

        res.json({ success: true, message: 'Location recorded in both tables' });
      });
    });
  });
});

// G. Fetch Nearby Promotions from Mobile App
app.get('/api/nearby-promotions', (req, res) => {
  const lat = parseFloat(req.query.lat);
  const lon = parseFloat(req.query.lon);
  const radius = parseFloat(req.query.radius) || 5;

  if (isNaN(lat) || isNaN(lon)) {
    return res.status(400).json({ error: 'Latitude and longitude are required!' });
  }

  const query = `
    SELECT s.name AS shop_name, s.address, s.latitude, s.longitude, 
           p.title AS promo_title, p.description AS promo_description, p.discount, p.valid_until
    FROM promotions p
    JOIN shops s ON p.shop_id = s.id
  `;

  db.query(query, (err, results) => {
    if (err) return res.status(500).json({ error: err.message });

    function getDistance(lat1, lon1, lat2, lon2) {
      const R = 6371;
      const dLat = (lat2 - lat1) * Math.PI / 180;
      const dLon = (lon2 - lon1) * Math.PI / 180;
      const a =
        Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
        Math.sin(dLon / 2) * Math.sin(dLon / 2);
      const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
      return R * c;
    }

    const filtered = results
      .map(item => {
        const distance = getDistance(lat, lon, item.latitude, item.longitude);
        const formattedDate = item.valid_until ? new Date(item.valid_until).toISOString().split('T')[0] : '';
        return {
          shop_name: item.shop_name,
          address: item.address,
          promo_title: item.promo_title,
          promo_description: item.promo_description,
          discount: item.discount,
          valid_until: formattedDate,
          distance_km: parseFloat(distance.toFixed(2))
        };
      })
      .filter(item => item.distance_km <= radius);

    res.json({ promotions: filtered });
  });
});

app.listen(3000, "0.0.0.0", () => {
  console.log("🚀 Server running on port 3000");
});