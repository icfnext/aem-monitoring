import React from 'react';
import $ from 'jquery';
import _ from 'lodash';
import './EventDashboard.css'

import TIME_CONSTANTS from './times'
import Header from './Header';
import EventsChart from './EventsChart'
import FacetBar from './FacetBar'
import FacetFilters from './FacetFilters'
import EventListing from './EventListing'

var EVENT_COUNT = "Event Count";

class EventDashboard extends React.Component {
    constructor() {
        super();
        this.state = {
            types: [],
            propertyList: [],
            stringPropertyList: [],
            realPropertyList: [],
            typeMappings: {},
            selectedType: null,
            selectedProperty: null,
            filters: [],
            eventData: {},
            selectedYAxis: "0",
            selectedTime: TIME_CONSTANTS.INDICES.HOUR
        };
        // TODO Pull initial state from URL params
        this.loadTypes();

    }
    loadTypes() {
        $.getJSON('/bin/monitoring/eventTypes.json', null, function (data) {
            var types = [];
            var typeMappings = {};
            $.each(data, function () {
                types.push(this.name);
                typeMappings[this.name] = this.properties;
            });
            this.setState({
                typeMappings: typeMappings,
                types: types
            });
            this.forceUpdate(function () {
                this.typeChanged(null, 0);
            }.bind(this));
        }.bind(this));
    }
    typeChanged(event, typeIndex) {
        if (typeIndex !== this.state.selectedType) {
            var currentProperties = this.state.typeMappings[this.state.types[typeIndex]];
            var newState = {};
            $.extend(newState, this.state, {
                selectedType: typeIndex,
                propertyList: currentProperties,
                selectedProperty: null,
                selectedYAxis: "0",
                filters: []
            });
            this.setPropertiesForType(newState);
            this.updateQuery(newState);
            this.setState(newState);
        }
    }
    setPropertiesForType(newState) {
        var stringPropertyList = [];
        var realPropertyList = [{name: EVENT_COUNT}];
        $.each(newState.propertyList, function () {
            if (this.string) {
                stringPropertyList.push(this);
            }
            if (this.real) {
                realPropertyList.push(this);
            }
        });
        $.extend(newState, {
            stringPropertyList: stringPropertyList,
            realPropertyList: realPropertyList
        })
    }
    propertyChanged(event, propIndex) {
        if (propIndex !== this.state.selectedProperty) {
            var newState = {};
            $.extend(newState, this.state, {
                selectedProperty: propIndex
            });
            this.updateQuery(newState);
            this.setState(newState);
        }
    }
    yAxisChanged(event, propIndex) {
        if (propIndex !== this.state.selectedYAxis) {
            var newState = {};
            $.extend(newState, this.state, {
                selectedYAxis: propIndex
            });
            this.updateQuery(newState);
            this.setState(newState);
        }
    }
    timeChanged(event, timeIndex) {
        if (timeIndex !== this.state.selectedTime) {
            var newState = {};
            $.extend(newState, this.state, {
                selectedTime: timeIndex
            });
            this.updateQuery(newState);
            this.setState(newState);
        }
    }
    addFacet(property, facetId) {
        this.addFilter(property + " = " + facetId);
        this.setState({
            selectedProperty: null
        });
    }
    addFilter(filterString) {
        var newState = {};
        $.extend(newState, this.state);
        let filters = newState.filters || [];
        filters.push(filterString);
        newState.filters = filters;
        this.updateQuery(newState);
        this.setState(newState);
    }
    facetRemoved(filter) {
        if (filter) {
            var newState = {};
            $.extend(newState, this.state);
            newState.filters = _.without(newState.filters || [], filter);
            this.updateQuery(newState);
            this.setState(newState);
        }
    }
    showEvents(startEpoch, endEpoch, facetIndex) {
        var stateCopy = {};
        $.extend(true, stateCopy, this.state);
        var type = stateCopy.types[stateCopy.selectedType];
        var facet = stateCopy.stringPropertyList[stateCopy.selectedProperty];
        var filters = stateCopy.filters;
        if (facet && facetIndex != null) {
            filters[facet.name] = stateCopy.chartData.facets[facetIndex].id;
        }
        var yAxis = stateCopy.realPropertyList[stateCopy.selectedYAxis];
        var args = [];
        if (type) {
            args.push("type=" + type);
        }
        if (yAxis && yAxis.name !== EVENT_COUNT) {
            args.push("y-axis=" + yAxis.name);
        }
        args.push("start-epoch=" + String(startEpoch));
        args.push("end-epoch=" + String(endEpoch));
        if (filters) {
            $.each(filters, function (index, value) {
                args.push("filter=" + encodeURIComponent(value))
            })
        }
        var url = encodeURI("/bin/monitoring/eventData.json?" + args.join("&"));
        $.getJSON(url, null, function (data) {
            this.setState({eventData: data});
        }.bind(this));
    }
    updateQuery(newState) {
        var type = newState.types[newState.selectedType];
        var facet = newState.stringPropertyList[newState.selectedProperty];
        var yAxis = newState.realPropertyList[newState.selectedYAxis];
        var filters = newState.filters;
        var time = TIME_CONSTANTS.TIMES_IN_MS[newState.selectedTime];
        var args = [];
        if (type) {
            args.push("type=" + type);
        }
        if (facet && facet.name) {
            args.push("facet=" + facet.name);
        }
        if (yAxis && yAxis.name !== EVENT_COUNT) {
            args.push("y-axis=" + yAxis.name);
        }
        if (time) {
            args.push("start-epoch=" + String((new Date()).getTime() - time)) ;
        }
        if (filters) {
            $.each(filters, function (index, value) {
                args.push("filter=" + encodeURIComponent(value))
            })
        }
        var url = encodeURI("/bin/monitoring/events.json?" + args.join("&"));
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
                        selectedType={this.state.selectedType}
                        typeChanged={this.typeChanged.bind(this)}
                        selectedTime={this.state.selectedTime}
                        timeChanged={this.timeChanged.bind(this)}
                        propertyList={this.state.realPropertyList}
                        selectedYAxis={this.state.selectedYAxis}
                        yAxisChanged={this.yAxisChanged.bind(this)}
                    />
                    <FacetBar
                        propertyList={this.state.stringPropertyList}
                        selectedProperty={this.state.selectedProperty}
                        propertyChanged={this.propertyChanged.bind(this)}
                        facet={this.state.stringPropertyList[this.state.selectedProperty]}
                        chartData={this.state.chartData}
                        addFacet={this.addFacet.bind(this)}
                    />
                    <div id="content">
                        <EventsChart
                            chartData={this.state.chartData}
                            selectedYAxis={this.state.selectedYAxis}
                            selectedTime={this.state.selectedTime}
                            pointClicked={this.showEvents.bind(this)}
                        />
                        <FacetFilters
                            filters={this.state.filters}
                            onClose={this.facetRemoved.bind(this)}
                            propertyList={this.state.propertyList}
                            addFilter={this.addFilter.bind(this)}
                        />
                    </div>
                </div>
                <div id="results">
                    <EventListing eventData={this.state.eventData}/>
                </div>
            </div>
        );
    }
}

export default EventDashboard