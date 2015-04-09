goog.provide('net.purplexsu.ColumnPage');

goog.require('goog.dom');
goog.require('net.purplexsu.Page');


/**
 * Article page class.
 * @constructor
 * @extends {net.purplexsu.Page}
 */
net.purplexsu.ColumnPage = function() {
  goog.base(this);
};
goog.inherits(net.purplexsu.ColumnPage, net.purplexsu.Page);



/**
 * Static function to call at the end of the JS.
 */
net.purplexsu.ColumnPage.render = function() {
  var page = new net.purplexsu.ColumnPage();
  page.render();
  goog.events.listen(window, goog.events.EventType.UNLOAD, function() {
    page.dispose();
  });
};


(function () {
  net.purplexsu.ColumnPage.render();
})();
