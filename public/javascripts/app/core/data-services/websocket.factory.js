/**
 * Created by thangle on 8/13/17.
 */
(function() {
  'use strict';

  angular
    .module('core')
    .factory('webSocketFactory', webSocketFactory);

  webSocketFactory.$inject = ['$websocket'];

  function webSocketFactory ($websocket) {
    var CHAT_MSG = 'chat-message';
    var FRIEND_MSG = 'friend-request';
    var WS_TIMEOUT = 20000;

    // open web socket connection.
    var ws = $websocket(getWebSocketUri("/socket"));
    // this id is used to keep track of timeout function
    var timerId = 0;

    /** Service for sending chat messages. */
    var Chat = {
      chatMessages: {},

      addMessage: function (msg, roomId) {
        var copyMsg = $.extend({}, msg);

        if (!this.chatMessages.hasOwnProperty(roomId)) {
          this.chatMessages[roomId] = [];
        }
        this.chatMessages[roomId].push(copyMsg);
      },

      send: function (msg, roomId, receivers) {
        var copyMsg = $.extend({}, msg);
        var sendData = { type: CHAT_MSG, content: copyMsg, roomId: roomId, receivers: receivers };
        ws.send(JSON.stringify(sendData));
      }
    };

    /** Service for sending friend request notifications. */
    var Notification = {
      notifications: [],

      send: function (msg, receivers) {
        var copyMsg = $.extend({}, msg);
        var sendData = { type: FRIEND_MSG, content: copyMsg , receivers: receivers };
        ws.send(JSON.stringify(sendData));
      },

      addNotification: function (request) {
        this.notifications.unshift(request);
      }
    };

    // callback when receive messages from the socket.
    ws.onMessage(function (msg) {
      var data = JSON.parse(msg.data);
      if (data.type === CHAT_MSG) {
        Chat.addMessage(data.content, data.roomId);
      } else if (data.type === FRIEND_MSG) {
        Notification.addNotification(data.content);
      }
    });


    ws.onOpen(function() {
      keepAlive();
    });


    ws.onClose(function() {
      cancelKeepAlive();
    });


    /** Send ping frame to the server to keep the websocket alive. */
    function keepAlive() {
      ws.send(JSON.stringify({ type: 'ping' }));
      timerId = setTimeout(keepAlive, WS_TIMEOUT);
    }


    /** Stop sending ping frame to the server. */
    function cancelKeepAlive() {
      if (timerId) {
        clearTimeout(timerId);
      }
    }


    return {
      Chat: Chat,
      Notification: Notification
    };
  }
})();