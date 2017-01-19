import React from 'react';
import $ from 'jquery';
import {SelectList, SelectListItem} from '../coral/Coral';
import COLORS from '../colors'

class MetricSelector extends React.Component {
    render() {
        var items = [];
        if (this.props.metricList) {
            let colorMapping = this.props.colorMapping;
            $.each(this.props.metricList, function (index, metric) {
                let color = COLORS[colorMapping[metric.id]];
                items.push(<SelectListItem key={index} text={metric.name} color={color} value={index}/>);
            });
        }
        return (
                <div>
                    <div>Show:</div>
                    <SelectList
                        placeholder="Metrics"
                        values={this.props.selectedMetric}
                        onChange={this.props.metricChanged}
                        id="facet-selector"
                        multiple
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
                    colorMapping={this.props.colorMapping}
                />
            </div>
        );
    }
}

export default MetricBar;