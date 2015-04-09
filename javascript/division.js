goog.provide('net.purplexsu.DivisionPage');

goog.require('goog.dom');
goog.require('net.purplexsu.Page');


/**
 * Article page class.
 * @constructor
 * @extends {net.purplexsu.Page}
 */
net.purplexsu.DivisionPage = function() {
  goog.base(this);
};
goog.inherits(net.purplexsu.DivisionPage, net.purplexsu.Page);



/**
 * Static function to call at the end of the JS.
 */
net.purplexsu.DivisionPage.render = function() {
  var page = new net.purplexsu.DivisionPage();
  page.render();
  goog.events.listen(window, goog.events.EventType.UNLOAD, function() {
    page.dispose();
  });
};


(function () {
  net.purplexsu.DivisionPage.render();
})();
