import React from 'react';
import {Table} from './coral/Coral';
import $ from 'jquery';
import _ from 'lodash';

class EventListing extends React.Component {
    constructor() {
        super();
        this.state = {
            show: false
        };
    }
    render() {
        return (
            <div id="modal-fullscreen" ref={(elem) => this.element = elem}>
                <div id="overlay-content">
                    <Table properties={this.props.eventData.propertyNames} rows={this.props.eventData.events} show={this.state.show}/>
                </div>
            </div>
        );
    }
    componentDidUpdate(prevProps, prevState) {
        if (!_.isEqual(prevProps.eventData, this.props.eventData)) {
            $('#modal-fullscreen').addClass("modal-backdrop-fullscreen");
        }
    }
    onHide() {
        $('#modal-fullscreen').removeClass("modal-backdrop-fullscreen");
    }
}

export default EventListing;