/**
 * @providesModule Twitter
 *
 * Modifications:
 * Copyright (C) 2016 Sony Interactive Entertainment Inc.
 * Licensed under the MIT License. See the LICENSE file in the project root for license information.
 */
'use strict';

var { NativeModules } = require('react-native');
var SMXTwitter = NativeModules.SMXTwitter;

module.exports = {
  login: function (cb) {
    SMXTwitter.login(cb);
  },
  fetchProfile: function (cb) {
    SMXTwitter.fetchProfile(cb);
  },
  logOut: function () {
    SMXTwitter.logOut();
  },
  getMyLists: function(options, cb) {
    SMXTwitter.getMyLists(options, cb);
  },
  getSubscribedLists: function(options, cb) {
    SMXTwitter.getSubscribedLists(options, cb);
  },
  getMemberLists: function(options, cb) {
    SMXTwitter.getMemberLists(options, cb);
  },
  getUserName: function(cb) {
    SMXTwitter.getUserName(cb);
  },
  createList: function(options, cb) {
    SMXTwitter.createList(options, cb);
  }
};
