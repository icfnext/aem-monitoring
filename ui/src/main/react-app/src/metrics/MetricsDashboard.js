import React from 'react';
import $ from 'jquery';

import TIME_CONSTANTS from '../times'
import COLORS from '../colors'
import Header from './Header';
import MetricsChart from './MetricsChart'
import MetricBar from './MetricBar'
import './MetricsDashboard.css'

class MetricsDashboard extends React.Component {
    constructor() {
        super();
        this.initialized = false;
        this.state = {
            selectedTypes: [],
            colorMapping: {}
        };
        // TODO Pull initial state from URL params
        this.loadTypes();

    }
    componentDidUpdate(prevProps, prevState) {
        if (!this.initialized) {
            return;
        }
        var oldUrl = this.generateUrl(prevState, prevProps);
        var newUrl = this.generateUrl(this.state, this.props);
        if (oldUrl !== newUrl) {
            this.updateQuery(newUrl);
        }
    }
    loadTypes() {
        $.getJSON('/bin/monitoring/metricTypes.json', null, function (data) {
            var types = [];
            $.each(data, function (key, value) {
                types.push({
                    name: key,
                    id: value
                });
            });
            this.setState({
                types: types
            });
            this.forceUpdate(function () {
                this.initialized = true;
                this.typeChanged(null, 0);
            }.bind(this));
        }.bind(this));
    }
    typeChanged(event, typeIndices) {
        let colorMappingCopy = {};
        let newColors = [];
        $.each(typeIndices, function (index, value) {
            let val = this.state.types[value].id;
            if (val in this.state.colorMapping) {
                colorMappingCopy[val] = this.state.colorMapping[val];
            } else {
                newColors.push(val);
            }
        }.bind(this));
        let remaining = new Array(COLORS.length);
        remaining.fill(true);
        $.each(colorMappingCopy, function (key, value) {
            remaining[value] = false;
        });
        let first = 0;
        // for each new id, find the first available color
        $.each(newColors, function (index, value) {
            for (; first < remaining.length; first++) {
                if (remaining[first]) {
                    remaining[first] = false;
                    colorMappingCopy[value] = first;
                    break;
                }
            }
        });

        this.setState({
            selectedTypes: typeIndices == null ? [] : typeIndices,
            colorMapping: colorMappingCopy
        });
    }
    generateUrl(newState, newProps) {
        let types = [];
        $.each(newState.selectedTypes, function (index, value) {
            types.push(newState.types[value]);
        });
        let time = TIME_CONSTANTS.TIMES_IN_MS[newProps.selectedTime];
        let args = [];
        $.each(types, function (index, value) {
            args.push("type=" + value.id);
        });
        if (time) {
            args.push("start-epoch=" + String((new Date()).getTime() - time)) ;
        }
        return encodeURI("/bin/monitoring/metrics.json?" + args.join("&"));
    }
    updateQuery(url) {
        $.getJSON(url, null, function (data) {
            this.setState({
                chartData: data,
                eventData: {}
            });
        }.bind(this));
    }
    render() {
        return (
            <div>
                <div id="wrapper">
                    <Header
                        types={this.state.types}
                        selectedType={this.state.selectedTypes}
                        typeChanged={this.typeChanged.bind(this)}
                        selectedTime={this.props.selectedTime}
                        timeChanged={this.props.timeChanged}
                    />
                    <MetricBar
                        metricList={this.state.types}
                        selectedMetric={this.state.selectedTypes}
                        metricChanged={this.typeChanged.bind(this)}
                        colorMapping={this.state.colorMapping}
                    />
                    <div id="content">
                        <MetricsChart
                            chartData={this.state.chartData}
                            selectedTime={this.props.selectedTime}
                            colorMapping={this.state.colorMapping}
                        />
                    </div>
                </div>
            </div>
        );
    }
}

export default MetricsDashboard