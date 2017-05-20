import React, { Component } from 'react';
import $ from 'jquery';
import './App.css';

import TIME_CONSTANTS from './times'
import VIEWS from './views'
import EventDashboard from './events/EventDashboard';
import MetricsDashboard from './metrics/MetricsDashboard';
import CounterDashboard from './counters/CounterDashboard';
import ConfigDashboard from './config/ConfigDashboard';

class App extends Component {
    constructor() {
        super();
        this.state = {
            clients: [],
            selectedTime: TIME_CONSTANTS.INDICES.HOUR
        };
    }
    render() {
        var items = [];
        if (this.state.view === VIEWS.METRICS) {
            items.push(<MetricsDashboard
                key="metrics"
                selectedTime={this.state.selectedTime}
                timeChanged={this.timeChanged.bind(this)}
            />)
        } else if (this.state.view === VIEWS.EVENTS) {
            items.push(<EventDashboard
                key="events"
                selectedTime={this.state.selectedTime}
                timeChanged={this.timeChanged.bind(this)}
            />)
        } else if (this.state.view === VIEWS.COUNTERS) {
            items.push(<CounterDashboard
                key="counters"
                selectedTime={this.state.selectedTime}
                timeChanged={this.timeChanged.bind(this)}
            />)
        } else if (this.state.view === VIEWS.CONFIG) {
            items.push(<ConfigDashboard
                key="config"
                clients={this.state.clients}
                update={this.fetchClients.bind(this)}
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
        const $switch = $('coral-buttongroup[name="monitoring-switch"]');
        $switch.click(function () {
            let val = $switch.find('button:selected').val();
            this.setState({
                view: val
            });
        }.bind(this));

        let val = $switch.find('button:selected').val();
        this.setState({
            view: val
        });

        this.fetchClients();
    }
    fetchClients() {
        $.getJSON('/bin/monitoring/clients.json', null, function (data) {
            const clients = [];
            $.each(data, function(key, value){
                let client = {
                    name: key
                };
                $.extend(client, value);
                clients.push(client);
            });
            this.setState({
                clients: clients
            })
        }.bind(this))
    }

}

export default App;
