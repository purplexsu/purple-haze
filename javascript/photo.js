goog.provide('net.purplexsu.PhotoPage');

goog.require('net.purplexsu.Page');


/**
 * Photo page class.
 * @constructor
 * @extends {net.purplexsu.Page}
 */
net.purplexsu.PhotoPage = function() {
  goog.base(this);
};
goog.inherits(net.purplexsu.PhotoPage, net.purplexsu.Page);


/** @override */
net.purplexsu.PhotoPage.prototype.renderInternal = function() {
  // empty so far.
};


/**
 * Static function to call at the end of the JS.
 */
net.purplexsu.PhotoPage.render = function() {
  var page = new net.purplexsu.PhotoPage();
  page.render();
  goog.events.listen(window, goog.events.EventType.UNLOAD, function() {
    page.dispose();
  });
};


(function () {
  net.purplexsu.PhotoPage.render();
})();