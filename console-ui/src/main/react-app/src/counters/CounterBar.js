import React from 'react';
import $ from 'jquery';
import {SelectList, SelectListItem} from '../coral/Coral';
import COLORS from '../colors'

class CounterSelector extends React.Component {
    render() {
        var items = [];
        if (this.props.counterList) {
            $.each(this.props.counterList, function (index, counter) {
                items.push(<SelectListItem key={index} text={counter.name} value={index}/>);
            });
        }
        return (
                <div>
                    <div>Show:</div>
                    <SelectList
                        placeholder="Counters"
                        value={this.props.selectedCounter}
                        onChange={this.props.counterChanged}
                        id="facet-selector"
                    >
                        {items}
                    </SelectList>
                </div>
            );
    }
    facetRemoved() {
        this.props.counterChanged(null, null);
    }
}

class CounterBar extends React.Component {
    render() {
        return (
            <div id="counter-nav">
                <CounterSelector
                    counterList={this.props.counterList}
                    selectedCounter={this.props.selectedCounter}
                    counterChanged={this.props.counterChanged}
                />
            </div>
        );
    }
}

export default CounterBar;