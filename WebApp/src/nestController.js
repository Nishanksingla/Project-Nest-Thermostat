app.controller("nestController", function ($scope, $http, $sce, client, esFactory) {  
    $scope.teamNum = 5
    $scope.logout = function () {
        delete window.sessionStorage.removeItem("login");
        window.location.assign("/");
    }
    $scope.predicted = null;
    $scope.series = [
        { name: "Humidity", data: [] },
        { name: "inTemp", data: [] },
        { name: "outTemp", data: [] },
        { name: "prediction", data: [] }
    ];
    
    Highcharts.setOptions({
        global: {
            useUTC: false
        }
    });
   
    $scope.teams=[1,2,3,4,5,6,7,8]
    
    $scope.predictionVals = function(){
            client.search({
            index: 'mldata',
            body: {
                "query": {
                    "match_all": {

                    }
                }
            }
        })
        .then(function (resp) {
            $scope.allPredictions = resp.hits.hits;
            angular.forEach($scope.allPredictions, function (values, key) {
                if (values._id === $scope.teamNum) {
                    $scope.predicted = values._source;
                }
            })
            $scope.Graph();
        }, function (error) {
            console.trace(error.message);
        });
    }
    
    $scope.predictionVals();
    
    setTimeout($scope.predictionVals, 4 * 60 * 1000);

    $scope.changeTeam = function(){
        $scope.series = [
            { name: "Humidity", data: [] },
            { name: "inTemp", data: [] },
            { name: "outTemp", data: [] },
            { name: "prediction", data: [] }
        ];
        
        angular.forEach($scope.allPredictions, function (values,key) {
            if (values._id===$scope.teamNum) {
                $scope.predicted = values._source;
            } 
        })
        $scope.Graph();
    }


    $scope.updateGraph = function () {
        client.search({
            index: 'data',
            body: {
                query: {
                    "filtered": {
                        "query": {
                            "query_string": {
                                "analyze_wildcard": true,
                                "query": "*"
                            }
                        },
                        "filter": {
                            "bool": {
                                "must": [
                                    {
                                        "range": {
                                            "@timestamp": {
                                                "gte": "now-5s",
                                                "lte": "now"
                                                // "format": "epoch_millis"
                                            }
                                        }
                                    },
                                    {
                                        "match": {
                                            "team": parseInt($scope.teamNum)
                                        }
                                    }
                                ],
                                "must_not": []
                            }
                        }
                    }
                },
                "size": 0,
                "aggs": {
                    "data": {
                        "date_histogram": {
                            "field": "@timestamp",
                            "interval": "5s",
                            "time_zone": "America/Los_Angeles",
                            "min_doc_count": 1
                        },
                        "aggs": {
                            "inTemp": {
                                "avg": {
                                    "field": "temp"
                                }
                            },
                            "Humidity": {
                                "avg": {
                                    "field": "humidity"
                                }
                            },
                            "outTemp": {
                                "avg": {
                                    "field": "outTemp"
                                }
                            }
                        }
                    }
                }
            }
        }).then(function (resp) {
            var dataES = resp.aggregations.data.buckets;
            angular.forEach(dataES, function (values) {
                $scope.charts.series[0].addPoint([values.key, values.Humidity.value]);
                $scope.charts.series[1].addPoint([values.key, values.inTemp.value]);
                $scope.charts.series[2].addPoint([values.key, values.outTemp.value]);
                if ($scope.predicted) {
                    var time = values.key_as_string.split('T')[1].split(':');

                    var out_norm = (values.outTemp.value - $scope.predicted.out_mean)/$scope.predicted.out_std;
                    var hour_norm = (parseInt(time[0]) - $scope.predicted.hour_mean)/$scope.predicted.hour_std;
                    var min_norm = (parseInt(time[1]) - $scope.predicted.min_mean)/$scope.predicted.min_std;
                    var humidity_norm = (values.Humidity.value - $scope.predicted.humidity_mean)/$scope.predicted.humidity_std;

                    var yVal = $scope.predicted.intercept + hour_norm * ($scope.predicted.hour) + min_norm * ($scope.predicted.min) + out_norm * ($scope.predicted.out) + humidity_norm * ($scope.predicted.humidity);

                    $scope.charts.series[3].addPoint([values.key, yVal]);
                }
            });
            setTimeout($scope.updateGraph, 5000);
        }, function (error) {
            console.trace(error.message);
        });
    }
   
    $scope.Graph = function () {
        client.search({
            index: 'data',
            body: {
                query: {
                    "filtered": {
                        "query": {
                            "query_string": {
                                "analyze_wildcard": true,
                                "query": "*"
                            }
                        },
                        "filter": {
                            "bool": {
                                "must": [
                                    {
                                        "range": {
                                            "@timestamp": {
                                                "gte": "now-15m",
                                                "lte": "now"
                                                // "format": "epoch_millis"
                                            }
                                        }
                                    },
                                    {
                                        "match": {
                                            "team": parseInt($scope.teamNum)
                                        }
                                    }
                                ],
                                "must_not": []
                            }
                        }
                    }
                },
                "size": 0,
                "aggs": {
                    "data": {
                        "date_histogram": {
                            "field": "@timestamp",
                            "interval": "5s",
                            "time_zone": "America/Los_Angeles",
                            "min_doc_count": 1
                        },
                        "aggs": {
                            "inTemp": {
                                "avg": {
                                    "field": "temp"
                                }
                            },
                            "Humidity": {
                                "avg": {
                                    "field": "humidity"
                                }
                            },
                            "outTemp": {
                                "avg": {
                                    "field": "outTemp"
                                }
                            }
                        }
                    }
                }
            }
        }).then(function (resp) {
            console.log(resp);

            var dataES = resp.aggregations.data.buckets;

            angular.forEach(dataES, function (values) {

                $scope.series[0].data.push({ x: values.key, y: values.Humidity.value });
                $scope.series[1].data.push({ x: values.key, y: values.inTemp.value });
                $scope.series[2].data.push({ x: values.key, y: values.outTemp.value });
                if ($scope.predicted) {
                    
                    var time = values.key_as_string.split('T')[1].split(':');
                    
                    var out_norm = (values.outTemp.value - $scope.predicted.out_mean)/$scope.predicted.out_std;
                    var hour_norm = (parseInt(time[0]) - $scope.predicted.hour_mean)/$scope.predicted.hour_std;
                    var min_norm = (parseInt(time[1]) - $scope.predicted.min_mean)/$scope.predicted.min_std;
                    var humidity_norm = (values.Humidity.value - $scope.predicted.humidity_mean)/$scope.predicted.humidity_std;
                    
                    var yVal = $scope.predicted.intercept + hour_norm * ($scope.predicted.hour) + min_norm * ($scope.predicted.min) + out_norm * ($scope.predicted.out) + humidity_norm * ($scope.predicted.humidity);

                    $scope.series[3].data.push({ x: values.key, y: yVal });
                }
            });
            debugger
            //---------HIGH CHARTS----------------------------------------------------------------------------------------------------        
            $scope.charts = new Highcharts.chart('container', {
                chart: {
                    type: 'line',
                    animation: Highcharts.svg,
                    marginRight: 20,
                    events: {
                        load: $scope.updateGraph
                    }
                },
                title: {
                    text: 'Nest Thermal Model',
                    x: -20 //center
                },
                subtitle: {
                    text: "For Team " + $scope.teamNum,
                    x: -20
                },
                xAxis: {
                    type: 'datetime',
                    tickPixelInterval: 50,
                },
                yAxis: {
                    title: {
                        text: 'Y-Axis'
                    },
                    plotLines: [{
                        value: 0,
                        width: 1,
                        color: '#808080'
                    }]
                },
                legend: {
                    layout: 'vertical',
                    align: 'right',
                    verticalAlign: 'top',
                    borderWidth: 0
                },
                series: $scope.series,
                exporting: {
                    enabled: false
                }
            });
            // ------------------------------------------------------------------------------------------------------------------------
        }, function (error) {
            console.trace(error.message);
        });
    }

});