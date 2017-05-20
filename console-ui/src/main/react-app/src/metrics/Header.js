import React from 'react';
import $ from 'jquery';
import TIME_CONSTANTS from '../times';
import {Select, SelectItem} from '../coral/Coral';

class MetricTypeSelector extends React.Component {
    render() {
        let items = [];
        if (this.props.types) {
            $.each(this.props.types, function (index, type) {
                items.push(<SelectItem key={index} text={type.name} value={index}/>)
            });
        }
        return (
            <div className="inline">
                <div>Show Type:</div>
                <Select
                    placeholder="Metric Type"
                    value={this.props.selectedType}
                    onChange={this.props.typeChanged}
                >
                    {items}
                </Select>
            </div>
        );
    }
}

class MetricTimeSelector extends React.Component {
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
                    <SelectItem text="One Month Ago" value={TIME_CONSTANTS.INDICES.MONTH}/>
                </Select>
            </div>
        );
    }
}

class Header extends React.Component {
    render() {
        return (
            <div>
                <div id="header">
                    <MetricTimeSelector
                        selectedTime={this.props.selectedTime}
                        timeChanged={this.props.timeChanged}
                    />
                </div>
            </div>
        );
    }
}

export default Header;