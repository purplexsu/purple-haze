goog.provide('net.purplexsu.IndexPage');

goog.require('goog.dom');
goog.require('net.purplexsu.Page');


/**
 * Article page class.
 * @constructor
 * @extends {net.purplexsu.Page}
 */
net.purplexsu.IndexPage = function() {
  goog.base(this);
};
goog.inherits(net.purplexsu.IndexPage, net.purplexsu.Page);



/**
 * Static function to call at the end of the JS.
 */
net.purplexsu.IndexPage.render = function() {
  var page = new net.purplexsu.IndexPage();
  page.render();
  goog.events.listen(window, goog.events.EventType.UNLOAD, function() {
    page.dispose();
  });
};


(function () {
  net.purplexsu.IndexPage.render();
})();
