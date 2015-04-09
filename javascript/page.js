goog.provide('net.purplexsu.Page');

goog.require('goog.Disposable');
goog.require('goog.dom');
goog.require('goog.dom.classlist');
goog.require('goog.events');
goog.require('goog.events.EventHandler');
goog.require('goog.events.KeyCodes');
goog.require('goog.userAgent');

/**
 * Define the base class for a page.
 * @constructor
 * @extends {goog.Disposable}
 */
net.purplexsu.Page = function() {

  /**
   * The event handler.
   * @type {goog.events.EventHandler}
   * @protected
   */
  this.handler = new goog.events.EventHandler(this);
};
goog.inherits(net.purplexsu.Page, goog.Disposable);


/**
 * The unique entry to render a page.
 */
net.purplexsu.Page.prototype.render = function() {
  this.handler.listen(
      window.document,
      goog.events.EventType.KEYDOWN,
      this.handleKeyDown_);
  this.setPageMode_();
  this.renderInternal();
};

net.purplexsu.Page.prototype.setPageMode_ = function() {
  var outline = goog.dom.getElementByClass('outline');
  if (!outline) {
    return;
  }
  if (goog.userAgent.MOBILE) {
    goog.dom.classlist.add(outline, 'mobile');
  } else {
    goog.dom.classlist.add(outline, 'pc');
  }
};

/**
 * Sub class should implement this function the render the specific page.
 */
net.purplexsu.Page.prototype.renderInternal = function() {
  // to be implemented.
};


/** @override */
net.purplexsu.Page.prototype.disposeInternal = function() {
  goog.base(this, 'disposeInternal');
};

/**
 * Switch to previous page or next page when left or right arrow key down.
 * @param {goog.events.Event} e The event.
 * @private
 */
net.purplexsu.Page.prototype.handleKeyDown_ = function(e) {
  var code = e.keyCode;
  if (code == goog.events.KeyCodes.LEFT) {
    // left arrow key
    if (!this.jumpToHref_("PreviousPage")) {
      this.jumpToHref_("PreviousArticle");
    }
  } else if (code == goog.events.KeyCodes.RIGHT) {
    // right arrow key
    if (!this.jumpToHref_("NextPage")) {
      this.jumpToHref_("NextArticle");
    }
  }
};


/**
 * Get element with given id, return false if fails. Otherwise, read the 'href' attribute of the
 * element and change the window.location to it.
 * @param {string} id The id of the element to find.
 * @return {boolean} true if given id could be found in document.
 * @private
 */
net.purplexsu.Page.prototype.jumpToHref_ = function(id) {
  var element = goog.dom.getElement(id);
  if (element) {
    window.location = element.href || window.location;
    return true;
  } else {
    return false;
  }
};
