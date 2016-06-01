app.controller("dashboardController", function ($scope, $http) {
    $scope.host = "54.187.15.61";
    $scope.port = 8001;
    $scope.buttonVal = "Connect";
    $scope.Status = "Disconnected";
    $scope.topics = [];
    $scope.subscribed_topic = "";
    $scope.notification = null;
    $scope.onoff = true;
    $scope.clientID = "Client_01";
    $scope.threshold = "20";
    $scope.doorOpen = false;
    $scope.messageObj = {};
    $scope.notificationCount = 0;

    $scope.connect_mosquitto = function () {
        debugger
        if ($scope.buttonVal === "Connect") {
            $scope.buttonVal = "Connecting..";
            $scope.mqtt_client = new Paho.MQTT.Client(
                $scope.host,
                $scope.port,
                $scope.clientID
            );

            $scope.mqtt_client.onConnectionLost = $scope.connectionLost;


            $scope.mqtt_client.onMessageArrived = $scope.messageArrived;

            var options = {
                // timeout: 10,
                // keepAliveInterval: 60,
                // cleanSession: true,
                onSuccess: $scope.onConnect,
                onFailure: function (message) {
                    debugger
                    $scope.Status = "Failed to connect";
                    console.log("error: " + message.errorMessage);
                    alert(message.errorMessage);
                    $scope.buttonVal = "Connect";
                    $scope.$apply();
                }
            };
            $scope.mqtt_client.connect(options);

        } else if ($scope.buttonVal === "Disconnect") {
            debugger
            $scope.Status = "Disconnected";
            $scope.buttonVal = "Connect";
            $scope.mqtt_client.disconnect();
            $scope.topics = [];
            $scope.messages = [];
        }
    }

    $scope.connectionLost = function (responseObject) {
        $scope.Status = "Disconnected";
        if (responseObject.errorCode !== 0) {
            console.log("onConnectionLost:" + responseObject.errorMessage);
        }
    };

    $scope.messageArrived = function (message) {

        console.log(message);
        $scope.messageObj = JSON.parse(message.payloadString);
        console.log($scope.messageObj);

        if ("notification" in $scope.messageObj && $scope.doorOpen === false) {
            debugger
            $scope.doorOpen = true
            $scope.notification = { Topic: message.destinationName, String: $scope.messageObj.notification };
            $scope.notificationCount = $scope.notificationCount + 1;

            var userID = window.sessionStorage.id;
            var address = "https://graph.facebook.com/" + userID + "/notifications";
            var tempdata = {};
            var appId = 924651857553242;
            var appSecret = "34190d10b7ab7f82638bc263e0274c94";
            debugger
            tempdata['access_token'] = appId + "|" + appSecret;
            tempdata['href'] = "http://localhost/";
            tempdata['template'] = "An object is detected";
            $http.post(address, tempdata)
                .success(function (data) {
                    alert("A notification is sent on facebook");
                })
                .error(function (data, status, headers, config) {
                    alert("failed to send facebook notification");
                });

        } else if ($scope.doorOpen === true && !("notification" in $scope.messageObj)) {
            $scope.doorOpen = false;
            $scope.notification = null;
        }
        
        $scope.$apply();
    };

    $scope.onConnect = function () {
        debugger
        $scope.Status = "Connected";
        $scope.buttonVal = "Disconnect";
        console.log("connected");
        $scope.mqtt_client.subscribe("SensorData");
        $scope.topics.push("SensorData");
        // $scope.mqtt_client.subscribe("Message");
        // $scope.topics.push("Message");
        // $scope.mqtt_client.subscribe("notification");
        // $scope.topics.push("notification");
        $scope.$apply();
    };

    $scope.setOnOff = function () {
        var message = new Paho.MQTT.Message(JSON.stringify({ "onoff": ($scope.onoff).toString(), "threshold": $scope.threshold }));
        message.destinationName = "commands";
        $scope.mqtt_client.send(message);
        debugger
    }

    $scope.setThreshold = function () {
        // $scope.threshold;
        var message = new Paho.MQTT.Message(JSON.stringify({ "onoff": ($scope.onoff).toString(), "threshold": $scope.threshold }));
        message.destinationName = "commands";
        $scope.mqtt_client.send(message);
        debugger
    }

    $scope.subscribe = function () {
        $scope.mqtt_client.subscribe($scope.subscribed_topic);
        $scope.topics.push($scope.subscribed_topic);
    };

    $scope.unSubscribe = function () {
        debugger
        $scope.mqtt_client.unsubscribe(this.topic);
        var index = $scope.topics.indexOf(this.topic);
        $scope.topics.splice(index, 1);
    };

    $scope.publish = function () {
        var message = new Paho.MQTT.Message($scope.message);
        message.destinationName = $scope.publish_topic;
        $scope.mqtt_client.send(message);
        // message.qos = 2;
        // message.retained = false;
    };

    $scope.logout = function () {
        delete window.sessionStorage.removeItem("login");
        window.location.assign("/");
    }

    // $scope.notification = function () {
    //     var userID = window.sessionStorage.id;
    //     var address = "https://graph.facebook.com/" + userID + "/notifications";
    //     var tempdata = {};
    //     var appId = 924651857553242;
    //     var appSecret = "34190d10b7ab7f82638bc263e0274c94";
    //     debugger
    //     tempdata['access_token'] = appId + "|" + appSecret;
    //     tempdata['href'] = "http://localhost/";
    //     tempdata['template'] = "Door is Open";
    //     $http.post(address, tempdata)
    //         .success(function (data) {
    //             debugger
    //         })
    //         .error(function (data, status, headers, config) {

    //         });
    // }

});