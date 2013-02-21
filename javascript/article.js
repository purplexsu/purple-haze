// JavaScript for aritcles

function externallinks() {  
 if (!document.getElementsByTagName) return; 
 var anchors = document.getElementsByTagName("a"); 
 for (var i=0; i<anchors.length; i++) { 
   var anchor = anchors[i]; 
   if (anchor.getAttribute("href") && 
       anchor.getAttribute("rel") == "external") {
     anchor.target = "_blank"; 
   }
 } 
}

function constructEmail() {
  if (!document.getElementById) return;
  var anchor = document.getElementById("contact");
  anchor.href = "mailto" + ":purplexsu" + "@gmai" + "l.com";
}


function pageCompleted() {
  externallinks();
  constructEmail();
  enableArrowKey();
}

window.onload = pageCompleted;
