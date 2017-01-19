import React from 'react';
import ReactCSSTransitionGroup from 'react-addons-css-transition-group'
import $ from 'jquery';
import './FacetFilters.css'
import {Button, TextField, Select, SelectItem, Tag} from '../coral/Coral';

class FacetFilter extends React.Component {
    render() {
        return (
            <Tag text={this.props.value} value={this.props.value} closable onClose={this.props.onClose}/>
        );
    }
}

var stringOperators = [
    "=",
    "LIKE",
    "!="
];

var realOperators = [
    "=",
    "!=",
    "<",
    "<=",
    ">",
    ">="
];

class FilterPropertyEditor extends React.Component {
    render() {
        let items = [];
        if (this.props.propertyList) {
            $.each(this.props.propertyList, function (index, property) {
                items.push(<SelectItem key={index} text={property.name} value={index}/>);
            });
        }
        return (
            <div className="inline">
                <div>Property:</div>
                <Select
                    placeholder="Property Name"
                    value={this.props.selectedIndex}
                    onChange={this.props.propertyChanged}
                >
                    {items}
                </Select>
            </div>
        );
    }
}

class FilterOperatorEditor extends React.Component {
    render() {
        let operators = [];
        if (this.props.operatorList) {
            $.each(this.props.operatorList, function (index, operator) {
                operators.push(<SelectItem key={index} text={operator} value={index}/>);
            });
        }
        return (
            <div className="inline">
                <div>Operator:</div>
                <Select
                    placeholder="Operator"
                    value={this.props.selectedIndex}
                    onChange={this.props.operatorChanged}
                    disabled={operators.length === 0}
                >
                    {operators}
                </Select>
            </div>
        );
    }
}

class FilterValueEditor extends React.Component {
    render() {
        return (
            <div className="inline">
                <div>Value:</div>
                <TextField placeholder="Value" onChange={this.props.onChange}/>
            </div>
        );
    }
}

class FilterEditor extends React.Component {
    constructor() {
        super();
        this.state = {
            propertyIndex: null,
            operatorIndex: null,
            operators: [],
            value: ''
        }
    }
    render() {
        return (
            <div id="filter-editor">
                <div className="inline" id="add-filter-label">Add Filter</div>
                <FilterPropertyEditor selectedIndex={this.state.propertyIndex} propertyList={this.props.propertyList} propertyChanged={this.propertyChanged.bind(this)}/>
                <FilterOperatorEditor selectedIndex={this.state.operatorIndex} operatorList={this.state.operators} operatorChanged={this.operatorChanged.bind(this)}/>
                <FilterValueEditor onChange={this.onValueChange.bind(this)}/>
                <div className="inline">
                    <div>&nbsp;</div>
                    <Button text="Save" onClick={this.addFilter.bind(this)}/>
                </div>
            </div>
        );
    }
    propertyChanged(element, index) {
        let property = this.props.propertyList[index];
        let operators = [];
        if (property != null) {
            if (property.string) {
                operators = stringOperators;
            }
            if (property.real) {
                operators = realOperators;
            }
        }
        this.setState({
            propertyIndex: index,
            operatorIndex: null,
            operators: operators
        })
    }
    operatorChanged(element, index) {
        this.setState({
            operatorIndex: index
        })
    }
    onValueChange(text) {
        this.setState({value: text});
    }
    addFilter() {
        let property = this.props.propertyList[this.state.propertyIndex];
        let operator = this.state.operators[this.state.operatorIndex];
        let value = this.state.value;
        this.props.hideEditor();
        this.props.addFilter([property.name, operator, value].join(' '));
    }
}

class FacetFilters extends React.Component {
    constructor() {
        super();
        this.state = {
            showAddFilter: false
        };
    }
    render() {
        if (!this.props.filters) {
            return null;
        } else {
            let filters = [];
            let onClose = this.props.onClose;
            $.each(this.props.filters, function (index, value) {
                filters.push(<FacetFilter key={index} value={value} onClose={onClose}/>);
            });
            let editors = [];
            if (this.state.showAddFilter) {
                editors.push(
                    <FilterEditor
                        key="filterEditor"
                        propertyList={this.props.propertyList}
                        addFilter={this.props.addFilter}
                        hideEditor={this.hideAddFilter.bind(this)}
                    />
                );
            }
            filters.push(<Tag key="add" icon="add" size="XS" id="addFilter" onClick={this.showAddFilter.bind(this)}/>);
            return (
                <div className="inline">
                    <div>Filters:</div>
                    <div id="facet-filters">
                        <ReactCSSTransitionGroup
                            transitionName="facet-filters"
                            transitionEnterTimeout={500}
                            transitionLeaveTimeout={300}>
                            {filters}
                        </ReactCSSTransitionGroup>
                    </div>
                </div>
            )
        }
    }
    showAddFilter() {
        this.setState({showAddFilter: true});
    }
    hideAddFilter() {
        this.setState({showAddFilter: false});
    }

}

export default FacetFilters;