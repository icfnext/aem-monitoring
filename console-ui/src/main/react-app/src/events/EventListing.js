import React from 'react';
import $ from 'jquery';
import _ from 'lodash';
import {Table} from '../coral/Coral';

class EventListing extends React.Component {
    constructor() {
        super();
        this.state = {
            show: true
        };
    }
    render() {
        return (
                    <Table properties={this.props.eventData.propertyNames} rows={this.props.eventData.events} show={this.state.show}/>
        );
    }
    componentDidUpdate(prevProps, prevState) {
        if (this.state.show !== true) {
            this.setState({
                show: true
            })
        }
    }
    onHide() {
    }
}

export default EventListing;