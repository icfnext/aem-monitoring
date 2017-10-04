import React from 'react';
import $ from 'jquery';

import TIME_CONSTANTS from '../times'
import Header from './Header';
import MetricsChart from './MetricsChart'
import MetricBar from './MetricBar'
import './MetricsDashboard.css'

class MetricsDashboard extends React.Component {
    constructor() {
        super();
        this.initialized = false;
        this.state = {
            selectedTypes: []
        };
        // TODO Pull initial state from URL params
        this.loadTypes();
    }
    componentDidUpdate(prevProps, prevState) {
        if (!this.initialized) {
            return;
        }
        let oldUrl = this.generateUrl(prevState, prevProps);
        let newUrl = this.generateUrl(this.state, this.props);
        if (oldUrl !== newUrl) {
            this.updateQuery(newUrl);
            this.setState({
                chartData: null
            });
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
                this.typeChanged(null, [0]);
            }.bind(this));
        }.bind(this));
    }
    typeChanged(event, typeIndices) {
        this.setState({
            selectedTypes: typeIndices == null ? [] : typeIndices
        });
    }
    generateUrl(newState, newProps) {
        let types = [];
        $.each(newState.selectedTypes, function (index, value) {
            types.push(newState.types[value]);
        });
        let time = TIME_CONSTANTS.TIMES_IN_MS[newProps.selectedTime];
        let args = [];
        if (types === null || types.length === 0) {
            return null;
        }
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
                    />
                    <div id="content">
                        <MetricsChart
                            chartData={this.state.chartData}
                            selectedTime={this.props.selectedTime}
                        />
                    </div>
                </div>
            </div>
        );
    }
}

export default MetricsDashboard