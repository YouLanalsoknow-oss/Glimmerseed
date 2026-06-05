function refreshServer() {
    fetch("/admin/api/server")
        .then(r => r.json())
        .then(d => {
            if (!d.success) return;
            const s = d.data;
            document.getElementById("serverTime").textContent = s.now;
        });
}

function refreshUsers() {
    fetch("/admin/api/stats")
        .then(r => r.json())
        .then(d => {
            if (!d.success) return;
            const st = d.data;
            document.getElementById("totalUsers").textContent = st.total_users;
            document.getElementById("totalPets").textContent = st.total_pets;
            document.getElementById("totalMessages").textContent = st.total_messages;
        });
    window.location.reload();
}

setInterval(function () {
    var el = document.getElementById("serverTime");
    if (el) {
        var now = new Date();
        el.textContent = now.getFullYear() + "-" +
            String(now.getMonth() + 1).padStart(2, "0") + "-" +
            String(now.getDate()).padStart(2, "0") + " " +
            String(now.getHours()).padStart(2, "0") + ":" +
            String(now.getMinutes()).padStart(2, "0") + ":" +
            String(now.getSeconds()).padStart(2, "0");
    }
}, 1000);
