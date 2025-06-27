function calculateSavings() {
    const currentBill = document.getElementById('currentBill').value;
    const resultContainer = document.getElementById('resultContainer');
    const monthlySavings = document.getElementById('monthlySavings');
    const yearlySavings = document.getElementById('yearlySavings');

    if (!currentBill || isNaN(currentBill) || currentBill <= 0) {
        alert('Por favor, insira um valor válido para sua conta de energia.');
        return;
    }

    const monthlyValue = currentBill * 0.60;
    const yearlyValue = monthlyValue * 12;

    monthlySavings.textContent = `R$ ${monthlyValue.toFixed(2)}`;
    yearlySavings.textContent = `R$ ${yearlyValue.toFixed(2)}`;
    resultContainer.style.display = 'block';
}

// Event Listeners
document.addEventListener('DOMContentLoaded', function() {
    const calculateBtn = document.getElementById('calculateBtn');
    if (calculateBtn) {
        calculateBtn.addEventListener('click', calculateSavings);
    }

    // Handle navbar scroll
    const navbar = document.querySelector('.navbar');
    if (navbar) {
        window.addEventListener('scroll', function() {
            if (window.scrollY > 50) {
                navbar.classList.add('navbar-scrolled');
            } else {
                navbar.classList.remove('navbar-scrolled');
            }
        });
    }

    console.log('BluVolt website initialized successfully!');
});