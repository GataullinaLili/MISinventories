// Общие функции для модальных окон
function openModal(modalId) {
    document.getElementById(modalId).style.display = 'flex';
}

function closeModal(modalId) {
    document.getElementById(modalId).style.display = 'none';
}

// Закрытие модалки по клику вне её
window.addEventListener('click', function(event) {
    if (event.target.classList.contains('modal-overlay')) {
        event.target.style.display = 'none';
    }
});

// Вспомогательная функция для копирования в буфер обмена
function copyToClipboard(text) {
    navigator.clipboard.writeText(text).then(function() {
        alert('Скопировано: ' + text);
    });
}