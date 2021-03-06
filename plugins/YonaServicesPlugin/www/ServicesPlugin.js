var exec = require("cordova/exec"),
  channel = require("cordova/channel");

exports.configureVPN = function(params) {
  return new Promise(function(resolve, reject) {
    cordova.exec(resolve, reject, "VpnPlugin", "configureVPN", [
      params.vpnProfilePath,
      params.vpnLoginID,
      params.vpnPassword
    ]);
  });
};

exports.startVPN = function(params) {
  return new Promise(function(resolve, reject) {
    cordova.exec(resolve, reject, "VpnPlugin", "startVPN", []);
  });
};

exports.VPNLogEnabled = function(params) {
  return new Promise(function(resolve, reject) {
    cordova.exec(resolve, reject, "VpnPlugin", "VPNLogEnabled", []);
  });
};

exports.toggleVPNLog = function(params) {
  return new Promise(function(resolve, reject) {
    cordova.exec(resolve, reject, "VpnPlugin", "toggleVPNLog", []);
  });
};

exports.stopVPN = function(params) {
  return new Promise(function(resolve, reject) {
    cordova.exec(resolve, reject, "VpnPlugin", "stopVPN", []);
  });
};

/**
 * Checks if user has usageaccess
 *
 * @return [ boolean ]
 */
exports.checkUsageAccess = function(userCallback) {
  return new Promise(function(resolve, reject) {
    cordova.exec(resolve, reject, "BackgroundMode", "checkUsageAccess", []);
  });
};

/**
 * Checks if user has usageaccess
 * if not open settings
 *
 * @return [ Void ]
 */
exports.getUsageAccess = function(userCallback) {
  return new Promise(function(resolve, reject) {
    cordova.exec(resolve, reject, "BackgroundMode", "getUsageAccess", []);
  });
};

/**
 * Send app activities to the server
 *
 * @return [ Void ]
 */
exports.postActivitiesToServer = function(userCallback) {
  return new Promise(function(resolve, reject) {
    cordova.exec(resolve, reject, "BackgroundMode", "postActivitiesToServer", []);
  });
};


/**
 * Activates the background mode. When activated the application
 * will be prevented from going to sleep while in background
 * for the next time.
 *
 * @return [ Void ]
 */
exports.enable = function() {
  if (this.isEnabled()) return;

  var fn = function() {
    exports._isEnabled = true;
  };

  cordova.exec(fn, null, "BackgroundMode", "enable", []);
};

/**
 * Deactivates the background mode. When deactivated the application
 * will not stay awake while in background.
 *
 * @return [ Void ]
 */
exports.disable = function() {
  if (!this.isEnabled()) return;

  var fn = function() {
    exports._isEnabled = false;
  };

  cordova.exec(fn, null, "BackgroundMode", "disable", []);
};

/**
 * Enable or disable the background mode.
 *
 * @param [ Bool ] enable The status to set for.
 *
 * @return [ Void ]
 */
exports.setEnabled = function(enable) {
  if (enable) {
    this.enable();
  } else {
    this.disable();
  }
};

/**
 * List of all available options with their default value.
 *
 * @return [ Object ]
 */
exports.getDefaults = function() {
  return this._defaults;
};

/**
 * The actual applied settings.
 *
 * @return [ Object ]
 */
exports.getSettings = function() {
  return this._settings || {};
};

/**
 * Overwrite the default settings.
 *
 * @param [ Object ] overrides Dict of options to be overridden.
 *
 * @return [ Void ]
 */
exports.setDefaults = function(overrides) {
  var defaults = this.getDefaults();

  for (var key in defaults) {
    if (overrides.hasOwnProperty(key)) {
      defaults[key] = overrides[key];
    }
  }

  if (this._isAndroid) {
    cordova.exec(null, null, "BackgroundMode", "configure", [defaults, false]);
  }
};

/**
 * Configures the notification settings for Android.
 * Will be merged with the defaults.
 *
 * @param [ Object ] options Dict of options to be overridden.
 *
 * @return [ Void ]
 */
exports.configure = function(options) {
  var settings = this.getSettings(),
    defaults = this.getDefaults();

  if (!this._isAndroid) return;

  if (!this._isActive) {
    console.log("BackgroundMode is not active, skipped...");
    return;
  }

  this._mergeObjects(options, settings);
  this._mergeObjects(options, defaults);
  this._settings = options;

  cordova.exec(null, null, "BackgroundMode", "configure", [options, true]);
};

/**
 * Enable GPS-tracking in background (Android).
 *
 * @return [ Void ]
 */
exports.disableWebViewOptimizations = function() {
  if (this._isAndroid) {
    cordova.exec(null, null, "BackgroundModeExt", "webview", []);
  }
};

/**
 * Disables battery optimazation mode for the app.
 *
 * @return [ Void ]
 */
exports.disableBatteryOptimizations = function() {
  if (this._isAndroid) {
    cordova.exec(null, null, "BackgroundModeExt", "battery", []);
  }
};

/**
 * Checks if there is an available intent for autostart for current device.
 *
 * @return [ boolean ]
 */
exports.checkAppStartSettings = function(options, userCallback) {
  return new Promise(function(resolve, reject) {
    cordova.exec(resolve, reject, "BackgroundModeExt", "checkappstart", []);
  });
};

/**
 * Opens the system settings dialog where the user can tweak or turn off any
 * custom app start settings added by the manufacturer if available.
 *
 * @param [ Object|Bool ] options Set to false if you dont want to display an
 *                                alert dialog first.
 *
 * @return [ Void ]
 */
exports.openAppStartSettings = function(options) {
  if (this._isAndroid) {
    cordova.exec(null, null, "BackgroundModeExt", "appstart", [options]);
  }
};

/**
 * Move app to background (Android only).
 *
 * @return [ Void ]
 */
exports.moveToBackground = function() {
  if (this._isAndroid) {
    cordova.exec(null, null, "BackgroundModeExt", "background", []);
  }
};

/**
 * Exclude the app from the recent tasks list (Android only).
 *
 * @return [ Void ]
 */
exports.excludeFromTaskList = function() {
  if (this._isAndroid) {
    cordova.exec(null, null, "BackgroundModeExt", "tasklist", []);
  }
};

/**
 * If the screen is off.
 *
 * @param [ Function ] fn Callback function to invoke with boolean arg.
 *
 * @return [ Void ]
 */
exports.isScreenOff = function(fn) {
  if (this._isAndroid) {
    cordova.exec(fn, null, "BackgroundModeExt", "dimmed", []);
  } else {
    fn(undefined);
  }
};

/**
 * If the mode is enabled or disabled.
 *
 * @return [ Boolean ]
 */
exports.isEnabled = function() {
  return this._isEnabled !== false;
};

/**
 * If the mode is active.
 *
 * @return [ Boolean ]
 */
exports.isActive = function() {
  return this._isActive !== false;
};

exports._listener = {};

/**
 * Register callback for given event.
 *
 * @param [ String ] event The event's name.
 * @param [ Function ] callback The function to be exec as callback.
 * @param [ Object ] scope The callback function's scope.
 *
 * @return [ Void ]
 */
exports.on = function(event, callback, scope) {
  if (typeof callback !== "function") return;

  if (!this._listener[event]) {
    this._listener[event] = [];
  }

  var item = [callback, scope || window];

  this._listener[event].push(item);
};

/**
 * Unregister callback for given event.
 *
 * @param [ String ] event The event's name.
 * @param [ Function ] callback The function to be exec as callback.
 *
 * @return [ Void ]
 */
exports.un = function(event, callback) {
  var listener = this._listener[event];

  if (!listener) return;

  for (var i = 0; i < listener.length; i++) {
    var fn = listener[i][0];

    if (fn == callback) {
      listener.splice(i, 1);
      break;
    }
  }
};

/**
 * @private
 *
 * Flag indicates if the mode is enabled.
 */
exports._isEnabled = false;

/**
 * @private
 *
 * Flag indicates if the mode is active.
 */
exports._isActive = false;

/**
 * @private
 *
 * Default values of all available options.
 */

exports._defaults = {
  title: "App is running in background",
  text: "Doing heavy tasks.",
  subText: "",
  bigText: false,
  resume: true,
  silent: false,
  hidden: true,
  color: undefined,
  icon: "icon",
  channelName: "cordova-plugin-background-mode",
  channelDescription: "cordova-plugin-background-moden notification",
  allowClose: false,
  closeIcon: "power",
  closeTitle: "Close",
  showWhen: true
};

/**
 * @private
 *
 * Merge settings with default values.
 *
 * @param [ Object ] options The custom options.
 * @param [ Object ] toMergeIn The options to merge in.
 *
 * @return [ Object ] Default values merged with custom values.
 */
exports._mergeObjects = function(options, toMergeIn) {
  for (var key in toMergeIn) {
    if (!options.hasOwnProperty(key)) {
      options[key] = toMergeIn[key];
      continue;
    }
  }

  return options;
};

/**
 * @private
 *
 * Setter for the isActive flag. Resets the
 * settings if the mode isnt active anymore.
 *
 * @param [ Boolean] value The new value for the flag.
 *
 * @return [ Void ]
 */
exports._setActive = function(value) {
  if (this._isActive == value) return;

  this._isActive = value;
  this._settings = value ? this._mergeObjects({}, this._defaults) : {};
};

/**
 * @private
 *
 * Initialize the plugin.
 *
 * Method should be called after the 'deviceready' event
 * but before the event listeners will be called.
 *
 * @return [ Void ]
 */
exports._pluginInitialize = function() {
  this._isAndroid = device.platform.match(/^android|amazon/i) !== null;
  this.setDefaults({});
};

// Called before 'deviceready' listener will be called
channel.onCordovaReady.subscribe(function() {
  channel.onCordovaInfoReady.subscribe(function() {
    exports._pluginInitialize();
  });
});
