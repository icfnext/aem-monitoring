import React, { Component } from 'react';
import $ from 'jquery';
import './App.css';

import TIME_CONSTANTS from './times'
import EventDashboard from './events/EventDashboard';
import MetricsDashboard from './metrics/MetricsDashboard';

class App extends Component {
    constructor() {
        super();
        this.state = {
            metrics: false,
            selectedTime: TIME_CONSTANTS.INDICES.HOUR
        };
    }
    render() {
        var items = [];
        if (this.state.metrics) {
            items.push(<MetricsDashboard
                key="metrics"
                selectedTime={this.state.selectedTime}
                timeChanged={this.timeChanged.bind(this)}
            />)
        } else {
            items.push(<EventDashboard
                key="events"
                selectedTime={this.state.selectedTime}
                timeChanged={this.timeChanged.bind(this)}
            />)
        }
        return (
            <div>
                {items}
            </div>
        );
    }
    timeChanged(event, timeIndex) {
        this.setState({
            selectedTime: timeIndex
        });
    }
    componentDidMount() {
        $('#monitoring-metrics-switch').click(function () {
            this.setState({
                metrics: true
            })
        }.bind(this));
        $('#monitoring-events-switch').click(function () {
            this.setState({
                metrics: false
            })
        }.bind(this));
    }

}

export default App;
