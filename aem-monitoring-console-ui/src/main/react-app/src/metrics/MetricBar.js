import React from 'react';
import $ from 'jquery';
import {SelectList, SelectListItem} from '../coral/Coral';
import COLORS from '../colors'

class MetricSelector extends React.Component {
    render() {
        var items = [];
        if (this.props.metricList) {
            $.each(this.props.metricList, function (index, metric) {
                items.push(<SelectListItem key={index} text={metric.name} value={index}/>);
            });
        }
        return (
                <div>
                    <div>Show:</div>
                    <SelectList
                        placeholder="Metrics"
                        value={this.props.selectedMetric}
                        onChange={this.props.metricChanged}
                        id="facet-selector"
                    >
                        {items}
                    </SelectList>
                </div>
            );
    }
    facetRemoved() {
        this.props.metricChanged(null, null);
    }
}

class MetricBar extends React.Component {
    render() {
        return (
            <div id="metrics-nav">
                <MetricSelector
                    metricList={this.props.metricList}
                    selectedMetric={this.props.selectedMetric}
                    metricChanged={this.props.metricChanged}
                />
            </div>
        );
    }
}

export default MetricBar;