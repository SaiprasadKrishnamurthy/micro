var stompClient = null;

function plot(mapData) {
    var body = mapData.body;

    var xValue = [];
    var yValue = [];
    var json = JSON.parse(mapData.body)
       for(var k in json) {
            xValue.push(k);
            yValue.push(json[k]);
       }

    var trace1 = {
      x: xValue,
      y: yValue,
      type: 'bar',
      text: yValue,
      textposition: 'auto',
      marker: {
        color: 'rgb(142,124,195)',
        opacity: 0.6,
        line: {
          color: 'rbg(8,48,107)',
          width: 1.5
        }
      }
    };

    var data = [trace1];

    var layout = {
      title: 'UK Border - Traveller Nationalities being precleared (Last 2 hours)',
      font:{
        family: 'Raleway, snas-serif'
      },
      showlegend: false,
      xaxis: {
        tickangle: -45
      },
      yaxis: {
        zeroline: false,
        gridwidth: 2
      },
      bargap :0.05
    };

    Plotly.newPlot('chartdiv', data, layout);
}

function connect() {
    var socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        console.log('Connected: ' + frame);
        stompClient.subscribe('/topic/preclearance', function (data) {
            plot(data);
        });
        sendName();
    });
}

function disconnect() {
    if (stompClient != null) {
        stompClient.disconnect();
    }
    console.log("Disconnected");
}

function sendName() {
    console.log("Sending...");
    stompClient.send("/app/hello", {});
}

connect();
