import React from 'react';
import $ from 'jquery';
import _ from 'lodash';
import './EventDashboard.css'

import TIME_CONSTANTS from '../times'
import Header from './Header';
import EventsChart from './EventsChart'
import FacetBar from './FacetBar'
import FacetFilters from './FacetFilters'
import EventListing from './EventListing'

var EVENT_COUNT = "Event Count";

class EventDashboard extends React.Component {
    constructor() {
        super();
        this.initialized = false;
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
            selectedYAxis: "0"
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
        $.getJSON('/bin/monitoring/eventTypes.json', null, function (data) {
            var typeIds = [];
            var types = [];
            var typeMappings = {};
            $.each(data, function (index) {
                typeIds.push(this.id);
                types.push(this.name);
                typeMappings[index] = this.properties;
            });
            this.setState({
                typeIds: typeIds,
                typeMappings: typeMappings,
                types: types
            });
            this.forceUpdate(function () {
                this.initialized = true;
                this.typeChanged(null, 0);
            }.bind(this));
        }.bind(this));
    }
    typeChanged(event, typeIndex) {
        if (typeIndex !== this.state.selectedType) {
            var currentProperties = this.state.typeMappings[typeIndex];
            var newState = {};
            $.extend(newState, this.state, {
                selectedType: typeIndex,
                propertyList: currentProperties,
                selectedProperty: null,
                selectedYAxis: "0",
                filters: []
            });
            this.setPropertiesForType(newState);
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
    propertyChanged(event, propArray) {
        if (propArray && propArray.length === 1) {
            let propIndex = propArray[0];
            if (propIndex !== this.state.selectedProperty) {
                this.setState({
                    selectedProperty: propIndex
                });
            }
        } else {
            this.setState({
                selectedProperty: null
            });
        }
    }
    yAxisChanged(event, propIndex) {
        if (propIndex !== this.state.selectedYAxis) {
            var newState = {};
            $.extend(newState, this.state, {
                selectedYAxis: propIndex
            });
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
        this.setState(newState);
    }
    facetRemoved(filter) {
        if (filter) {
            var newState = {};
            $.extend(newState, this.state);
            newState.filters = _.without(newState.filters || [], filter);
            this.setState(newState);
        }
    }
    showEvents(startEpoch, endEpoch, facetIndex) {
        var stateCopy = {};
        $.extend(true, stateCopy, this.state);
        var type = stateCopy.typeIds[stateCopy.selectedType];
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
    generateUrl(newState, newProps) {
        var type = newState.typeIds[newState.selectedType];
        var facet = newState.stringPropertyList[newState.selectedProperty];
        var yAxis = newState.realPropertyList[newState.selectedYAxis];
        var filters = newState.filters;
        var time = TIME_CONSTANTS.TIMES_IN_MS[newProps.selectedTime];
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
        return encodeURI("/bin/monitoring/events.json?" + args.join("&"));
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
                        selectedType={this.state.selectedType}
                        typeChanged={this.typeChanged.bind(this)}
                        selectedTime={this.props.selectedTime}
                        timeChanged={this.props.timeChanged}
                        realPropertyList={this.state.realPropertyList}
                        stringPropertyList={this.state.stringPropertyList}
                        selectedYAxis={this.state.selectedYAxis}
                        yAxisChanged={this.yAxisChanged.bind(this)}
                        filters={this.state.filters}
                        onClose={this.facetRemoved.bind(this)}
                        addFilter={this.addFilter.bind(this)}
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
                            selectedTime={this.props.selectedTime}
                            pointClicked={this.showEvents.bind(this)}
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