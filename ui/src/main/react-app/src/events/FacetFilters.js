import React from 'react';
import ReactCSSTransitionGroup from 'react-addons-css-transition-group'
import $ from 'jquery';
import './FacetFilters.css'
import {Tag} from '../coral/Coral';

class FacetFilter extends React.Component {
    render() {
        return (
            <Tag text={this.props.value} value={this.props.value} closable onClose={this.props.onClose}/>
        );
    }
}

class FacetFilters extends React.Component {
    constructor() {
        super();
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
            filters.push(<Tag key="add" icon="add" size="XS" id="addFilter" onClick={this.props.showAddFilter}/>);
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

}

export default FacetFilters;