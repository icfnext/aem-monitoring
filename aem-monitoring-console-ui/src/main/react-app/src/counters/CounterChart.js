import React from 'react';
import Chart from "chart.js";
import moment from 'moment';
import $ from 'jquery';
import _ from 'lodash';

import COLORS from '../colors'

var timeAxes = {
    "0": "second",
    "1": "minute",
    "2": "hour",
    "3": "day",
};

function getDataset(data, label, colorIndex, lineOnly) {
    var dataset = {
        label: label,
        fill: !lineOnly,
        showLine: true,
        lineTension: 0.2,
        borderCapStyle: 'butt',
        borderDash: [],
        borderDashOffset: 0.0,
        borderWidth: lineOnly ? 2 : 1,
        borderJoinStyle: 'bevel',
        pointBackgroundColor: "#fff",
        pointBorderWidth: 1,
        pointHoverRadius: 3,
        pointHoverBorderWidth: 1,
        pointRadius: 0,
        pointHitRadius: 10,
        borderSkipped: 'bottom',
        data: data,
        spanGaps: false
    };
    return applyColor(dataset, colorIndex, lineOnly)
}

function applyColor(dataset, colorIndex, lineOnly) {
    let color = COLORS.getColor(colorIndex, 0.99);
    let lightColor = COLORS.getColor(colorIndex, 0.8);
    dataset.borderColor = color;
    dataset.pointBorderColor = color;
    dataset.pointHoverBackgroundColor = lightColor;
    dataset.pointHoverBorderColor = color;
    if (!lineOnly) {
        dataset.backgroundColor = lightColor;
    } else {
        dataset.backgroundColor = "rgba(1,1,1,0.01)";
    }
    return dataset;
}

class CounterChart extends React.Component {
    render() {
        return (
            <canvas id="chart" width="400" height="180" ref={(elem) => this.element = elem}/>
        );
    }
    componentDidMount() {
        var ctx = $(this.element);
        this.datasets = [];
        this.chart = new Chart(ctx, {
            type: 'line',
            data: {
                datasets: this.datasets
            },
            options: {
                legend: {
                    display: false
                },
                tooltips: {
                    callbacks: {
                        title: function (title) {
                            let epoch = Number(title[0].xLabel);
                            return moment(epoch).format("M/D/YYYY hh:mm");
                        }
                    }
                },
                hover: {
                    mode: 'nearest',
                    interset: true
                },
                scales: {
                    xAxes: [{
                        type: 'time',
                        time: {
                            displayFormats: {
                                minute: 'h:mm a'
                            },
                            minUnit: 'minute'
                        }
                    }],
                    yAxes: [{
                        ticks: {
                            suggestedMin: 0
                        },
                        stacked: true
                    }]
                }
            }
        });
    }
    updateChart(data) {
        var datasets = this.datasets;
        datasets.length = 0;
        if(data) {
            var facets = data.facets;
            var timeSeries;
            if (facets) {
                $.each(facets, function (index) {
                    let type = this.id;
                    let current = null;
                    for (let i = 0; i < this.timeSeries.points.length; i++) {
                        let point = this.timeSeries.points[i];
                        if (point.count !== 0) {
                            if (current === null) {
                                current = [];
                            }
                            current.push({'x': point.epoch, 'y': point.average});
                        } else {
                            if (current) {
                                datasets.push(getDataset(current, "", index, true));
                                current = null;
                            }
                        }
                    }
                    if (current) {
                        datasets.push(getDataset(current, "", index, true));
                    }
                });
            } else if (data && data.points) {
                let current = null;
                for (let i = 0; i < data.points.length; i++) {
                    let point = data.points[i];
                    if (point.count !== 0) {
                        if (current === null) {
                            current = [];
                        }
                        current.push({'x': point.epoch, 'y': point.average});
                    } else {
                        if (current) {
                            datasets.push(getDataset(current, "", datasets.length, true));
                            current = null;
                        }
                    }
                }
                if (current) {
                    datasets.push(getDataset(current, "", datasets.length, true));
                }
            }
            this.chart.options.scales.yAxes[0].stacked = false;
            this.chart.options.scales.xAxes[0].time.minUnit = timeAxes[this.props.selectedTime];
            //this.chart.options.scales.xAxes[0].time.min = timeSeries[0].x;
            //this.chart.options.scales.xAxes[0].time.max = timeSeries[timeSeries.length - 1].x;
        }
        this.chart.update();
    }
    componentDidUpdate(prevProps, prevState) {
        var data = this.props.chartData;
        if (data && !_.isEqual(data, prevProps.chartData)) {
            this.updateChart(data);
        }
    }
}

export default CounterChart;