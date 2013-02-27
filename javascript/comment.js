// JavaScript for comment files
var INPUT_ID_ALERT = "\u8bf7\u586b\u5199\u60a8\u7684ID\u3002";
var EMPTY_TEXT_AREA_COMMENT_ALERT = "\u7559\u8a00\u4e0d\u80fd\u4e3a\u7a7a\u3002";
var INVALID_TEXT_AREA_COMMENT_ALERT = "\u4e3a\u9632\u6b62\u5783\u573e\u7559" +
    "\u8a00\uff0c\u60a8\u53ea\u80fd\u5728\u6bcf\u6b21\u7559\u8a00" +
    "\u91cc\u5f15\u7528\u4e00\u4e2a\u8d85\u94fe\u63a5\u5730\u5740" +
    "\uff08http://\uff09\u3002\n\u540c\u65f6\uff0c\u8fd9\u4e2a\u5730" +
    "\u5740\u53ea\u4f1a\u4ee5\u7eaf\u6587\u672c\u7684\u5f62\u5f0f\u663e\u793a\u3002";
var INPUT_CAPCHAR_ALERT = "\u8bf7\u586b\u5199\u9a8c\u8bc1\u7801\u3002";

function PageCompleted() {
  var commentForm = document.getElementById("CommentForm");
  if (commentForm) {
    commentForm.onsubmit = VerifyForm;
  }
  var commentInput = document.getElementById("comment");
  if (commentInput) {
    commentInput.value = "";
  }

}
function VerifyForm() {
  var idInput = document.getElementById("id");
  if (idInput && (idInput.value == null || idInput.value == "")) {
    window.alert(INPUT_ID_ALERT);
    idInput.focus();
    return false;
  }
  var siteInput = document.getElementById("site");
  if (siteInput && siteInput.value != null && siteInput.value != "") {
    var pattern = new RegExp("^(http://|https://|ftp://).*\..*$");
    if(!pattern.test(siteInput.value)) {
      siteInput.value = "http://" + siteInput.value;
      }
  }
  var commentInput = document.getElementById("comment");
  if (commentInput) {
    var text = commentInput.value;
    if (text == null || text == "") {
      window.alert(EMPTY_TEXT_AREA_COMMENT_ALERT);
      commentInput.focus();
      return false;
    } else {
      var pattern = new RegExp("http://|https://", "g");
      var count = 0;
      while(pattern.exec(text) != null){
        count++;
      }
      if(count > 1){
        window.alert(INVALID_TEXT_AREA_COMMENT_ALERT);
        commentInput.focus();
        return false;
      }
    }
  }
  var capcharInput = document.getElementById("capchar");
  if (capcharInput && (capcharInput.value == null || capcharInput.value == "")) {
    window.alert(INPUT_CAPCHAR_ALERT);
    capcharInput.focus();
    return false;
  }
}
window.onload = PageCompleted;
