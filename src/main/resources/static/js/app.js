document.addEventListener("DOMContentLoaded", () => {
    const qtyInputs = document.querySelectorAll(".qty-input");
    const totalEl = document.getElementById("estimated-total");
    if (qtyInputs.length && totalEl) {
        const recalc = () => {
            let total = 0;
            qtyInputs.forEach((input) => {
                const qty = Number(input.value || 0);
                const price = Number(input.dataset.price || 0);
                total += qty * price;
            });
            totalEl.textContent = total.toFixed(2);
        };
        qtyInputs.forEach((input) => input.addEventListener("input", recalc));
        recalc();
    }

    const chart = document.getElementById("dashboard-chart");
    if (chart && chart.getContext) {
        const pending = Number(chart.dataset.pending || 0);
        const completed = Number(chart.dataset.completed || 0);
        const total = Number(chart.dataset.total || 0);
        const other = Math.max(total - pending - completed, 0);
        const ctx = chart.getContext("2d");
        const values = [pending, completed, other];
        const labels = ["Pending", "Completed", "Other"];
        const colors = ["#c2410c", "#166534", "#b45309"];
        const sum = values.reduce((a, b) => a + b, 0) || 1;
        let start = -Math.PI / 2;
        const cx = chart.width / 2;
        const cy = chart.height / 2;
        const radius = Math.min(cx, cy) - 10;
        values.forEach((value, index) => {
            const slice = (value / sum) * Math.PI * 2;
            ctx.beginPath();
            ctx.moveTo(cx, cy);
            ctx.arc(cx, cy, radius, start, start + slice);
            ctx.closePath();
            ctx.fillStyle = colors[index];
            ctx.fill();
            start += slice;
        });
        ctx.fillStyle = "#1f1a16";
        ctx.font = "14px Segoe UI";
        labels.forEach((label, index) => {
            ctx.fillText(`${label}: ${values[index]}`, 12, 18 + index * 18);
        });
    }
});
