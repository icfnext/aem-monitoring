import React from 'react';
import {Select, SelectItem} from './coral/Coral';
import $ from 'jquery';
import TIME_CONSTANTS from './times'

class EventTypeSelector extends React.Component {
    render() {
        var items = [];
        if (this.props.types) {
            $.each(this.props.types, function (index, type) {
                items.push(<SelectItem key={index} text={type} value={index}/>)
            });
        }
        return (
            <div className="inline">
                <div>Show Type:</div>
                <Select
                    placeholder="Event Type"
                    value={this.props.selectedType}
                    onChange={this.props.typeChanged}
                >
                    {items}
                </Select>
            </div>
        );
    }
}

class YAxisSelector extends React.Component {
    render() {
        var items = [];
        if (this.props.propertyList) {
            $.each(this.props.propertyList, function (index, property) {
                items.push(<SelectItem key={index} text={property.name} value={index}/>);
            });
        }
        return this.props.propertyList.length === 0 ? null : (
            <div className="inline">
                <div>Y-Axis:</div>
                <Select
                    placeholder="Y-Axis"
                    value={this.props.selectedProperty}
                    onChange={this.props.propertyChanged}
                >
                    {items}
                </Select>
            </div>
        );
    }
}

class EventTimeSelector extends React.Component {
    render() {
        return (
            <div className="inline">
                <div>Since:</div>
                <Select
                    placeholder="Time Period"
                    value={this.props.selectedTime}
                    onChange={this.props.timeChanged}
                >
                    <SelectItem text="One Minute Ago" value={TIME_CONSTANTS.INDICES.MINUTE}/>
                    <SelectItem text="One Hour Ago" value={TIME_CONSTANTS.INDICES.HOUR}/>
                    <SelectItem text="One Day Ago" value={TIME_CONSTANTS.INDICES.DAY}/>
                    <SelectItem text="One Week Ago" value={TIME_CONSTANTS.INDICES.WEEK}/>
                </Select>
            </div>
        );
    }
}

class Header extends React.Component {
    render() {
        return (

            <div id="header">
                <EventTypeSelector
                    types={this.props.types}
                    selectedType={this.props.selectedType}
                    typeChanged={this.props.typeChanged}
                />
                <EventTimeSelector
                    selectedTime={this.props.selectedTime}
                    timeChanged={this.props.timeChanged}
                />
                <YAxisSelector
                    propertyList={this.props.propertyList}
                    selectedProperty={this.props.selectedYAxis}
                    propertyChanged={this.props.yAxisChanged}
                />
            </div>
        );
    }
}

export default Header;