var flag = false;
function display_alert(classname){
	var elements = document.getElementsByClassName(classname);
	if (flag == false){
		for (var i in elements) {
			 if (elements.hasOwnProperty(i)) {
			  elements[i].style.width="141px";
			 }
		}
		flag = true;
	}
	else if (flag == true){
		for (var i in elements) {
			  if (elements.hasOwnProperty(i)) {
				  elements[i].style.width="inherit";
			  }
			}
		flag = false;
	}
}