document.getElementById('sumForm').addEventListener('submit', function(event) {
    event.preventDefault();
    var formData = new FormData(this);
    fetch('http://127.0.0.1:8000/calcular', {
        method: 'POST',
        body: formData
    })
    .then(response => response.text())
    .then(data => {
        document.getElementById('result').innerText = "La suma total es: " + data;
    })
    .catch(error => console.error('Error:', error));
    
});

