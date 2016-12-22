import React from 'react';
import {Button, SelectList, SelectListItem} from './coral/Coral';
import $ from 'jquery';
import COLORS from './colors'

class EventFacetSelector extends React.Component {
    render() {
        var items = [];
        if (this.props.propertyList) {
            $.each(this.props.propertyList, function (index, property) {
                items.push(<SelectListItem key={index} text={property.name} value={index}/>);
            });
        }
        return this.props.propertyList.length === 0
            ? null
            : this.props.selectedProperty
            ?
            (
                <Button
                    text={this.props.propertyList[this.props.selectedProperty].name}
                    onClick={this.facetRemoved.bind(this)}
                    icon="closeCircle"
                    iconSize="XS"
                    block/>
            )
            :
            (
                <div>
                    <div>Facet By:</div>
                    <SelectList
                        placeholder="Facet"
                        value={this.props.selectedProperty}
                        onChange={this.props.propertyChanged}
                        id="facet-selector"
                    >
                        {items}
                    </SelectList>
                </div>
            );
    }
    facetRemoved() {
        this.props.propertyChanged(null, null);
    }
}

class Facet extends React.Component {
    render() {
        let color = 'rgba(' + COLORS[this.props.facetIndex] + ',0.9)';
        let style = {
            'backgroundColor': color
        };
        let title = String(this.props.count) + ": " + this.props.name;
        return (
            <li ref={(elem) => this.element = elem} style={style} key={this.props.name} title={title}>
                <div className="facet-label"><div className="facet-count">{this.props.count}</div><span className="facet-title">{this.props.name}</span></div>
            </li>
        );
    }
    componentDidMount() {
        $(this.element).click(function () {
            this.props.addFacet(this.props.facet.name, this.props.name)
        }.bind(this));
    }
}

class FacetLegend extends React.Component {
    render() {
        if (this.props.selectedProperty === null || !this.props.chartData || !this.props.chartData.facets) {
            return null;
        } else {
            let facets = [];
            let facetIndex = 0;
            let addFacet = this.props.addFacet;
            let facet = this.props.facet;
            $.each(this.props.chartData.facets, function () {
                facets.push(<Facet
                    facet={facet}
                    name={this.id}
                    count={this.timeSeries.count}
                    facetIndex={facetIndex++}
                    addFacet={addFacet}
                    key={this.id}
                />)
            });
            return (
                <ul id="facet-legend">{facets}</ul>
            );
        }
    }
}

class FacetBar extends React.Component {
    render() {
        return (
            <div id="nav">
                <EventFacetSelector
                    propertyList={this.props.propertyList}
                    selectedProperty={this.props.selectedProperty}
                    propertyChanged={this.props.propertyChanged}
                />
                <FacetLegend
                    facet={this.props.facet}
                    selectedProperty={this.props.selectedProperty}
                    chartData={this.props.chartData}
                    addFacet={this.props.addFacet}
                />
            </div>
        );
    }
}

export default FacetBar;