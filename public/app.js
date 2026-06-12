const BASE_URL = 'http://172.20.10.3:3000/api/admin';

async function loadShopLocations() {
    try {
        const response = await fetch(`${BASE_URL}/shops`);
        const shops = await response.json();
        const tbody = document.querySelector('#shopTable tbody');
        if (!tbody) return;

        tbody.innerHTML = '';
        shops.forEach(shop => {
            tbody.innerHTML += `<tr><td>${shop.name}</td><td>${shop.address}</td><td>${shop.latitude}</td><td>${shop.longitude}</td></tr>`;
        });
    } catch (error) {
        console.error("Gagal memuatkan data kedai:", error);
    }
}

async function loadPromotions() {
    try {
        const response = await fetch(`${BASE_URL}/promotions`);
        const promotions = await response.json();
        const container = document.getElementById('promotionsList');
        if (!container) return;

        container.innerHTML = '';
        promotions.forEach(promo => {
            container.innerHTML += `<div class="promo-box">${promo.display_text}</div>`;
        });
    } catch (error) {
        console.error("Gagal memuatkan promosi:", error);
    }
}

async function loadUserLocations() {
    try {
        const response = await fetch(`${BASE_URL}/user-locations`);
        const locations = await response.json();
        const tbody = document.querySelector('#userLocationTable tbody');
        if (!tbody) {
            console.error("Error: Element '#userLocationTable tbody' not found in HTML!");
            return;
        }

        tbody.innerHTML = '';
        if (locations.length === 0) {
            tbody.innerHTML = `<tr><td colspan="4" style="text-align:center; color:red;">No location data found in the database.</td></tr>`;
            return;
        }

        locations.forEach(loc => {
            const rawTime = loc.time || loc.recorded_at || loc.checkin_time;
            const formattedTime = rawTime ? new Date(rawTime).toLocaleString('ms-MY') : 'N/A';

            tbody.innerHTML += `
                <tr>
                    <td><strong>${loc.user || loc.user_id}</strong></td>
                    <td>${loc.latitude}</td>
                    <td>${loc.longitude}</td>
                    <td>${formattedTime}</td>
                </tr>`;
        });
    } catch (error) {
        console.error("Failed to load user location data:", error);
    }
}

const notificationForm = document.querySelector('#notificationForm');
if (notificationForm) {
    notificationForm.addEventListener('submit', async (e) => {
        e.preventDefault();

        const email = document.querySelector('#notiEmail').value;
        const title = document.querySelector('#notiTitle').value;
        const message = document.querySelector('#notiMessage').value;

        try {
            const response = await fetch(`${BASE_URL}/send-notification`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ email, title, message })
            });

            const result = await response.json();

            if (response.ok) {
                alert(`Message Sent Successfully! \nNotification processed for email owner: ${email}`);
                notificationForm.reset();
            } else {
                alert(`Fail: ${result.error}`);
            }
        } catch (error) {
            console.error("Communication error between browser and server:", error);
            alert("Error: Unable to contact backend server.");
        }
    });
}

window.onload = function () {
    loadShopLocations();
    loadPromotions();
    loadUserLocations();

    setInterval(function () {
        console.log("Checking new location data...");
        loadUserLocations();
    }, 5000);
};