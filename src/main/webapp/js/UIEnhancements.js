/*var flag = false;
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

if (person!=null)
  	{
  		x=person;
  		document.write('<link rel="stylesheet" type="text/css" href=x />');

  	}


	<link href="${rootURL}/plugin/build-pipeline-plugin/css/main.css" type="text/css" rel="stylesheet" />

	<script type='text/javascript'>
	var url=prompt("Please enter a URL for css","${rootURL}/plugin/build-pipeline-plugin/css/main.css");

	if (url!=null)
  	{
  		document.write('<link rel="stylesheet" type="text/css" href=" '+url+' " />');

  	}

</script>

*/

function display_alert(classname){
	var link = document.getElementById(classname);
	link.setAttribute("href", "${rootURL}/plugin/build-pipeline-plugin/css/UIEnhancements.css");
	location.reload();

	}