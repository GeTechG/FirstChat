var xhr = new XMLHttpRequest();
var xhr1 = new XMLHttpRequest();
xhr.open('GET', 'http://localhost:10000/?name=Gena&id=6546546456');
xhr1.open('GET', 'http://localhost:10000/chat');
function test() {
    xhr.send();
    xhr1.send();
}
xhr.onreadystatechange = function() {
        if (xhr.readyState == 4 && xhr.status == 200)
            alert(xhr.responseText);
}
xhr1.onreadystatechange = function() {
        if (xhr1.readyState == 4 && xhr1.status == 200)
            alert(xhr1.responseText);
}
