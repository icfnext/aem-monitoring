import React from 'react';
import $ from 'jquery';

var buttonProps = [
    "block",
    "disabled",
    "selected"
];

class Button extends React.Component {
    render() {
        let attrs = {};
        $.extend(attrs,  this.props.attrs);
        for (let attr of buttonProps) {
            let value = this.props[attr];
            if (value) {
                attrs[attr] = value;
            }

        }
        return (
            <button is="coral-button" icon={this.props.icon} iconSize={this.props.iconSize} {...attrs} ref={(elem) => this.element = elem}>
                <coral-icon/>
                <coral-button-label class="coral-Button-label"> {this.props.text}</coral-button-label>
            </button>
        );
    }
    componentDidMount() {
        $(this.element).on('click', function (e) {
            if (this.props.onClick) {
                this.props.onClick(this);
            }
        }.bind(this));
    }
}

var textFieldProps = [
    'name',
    'value',
    'placeholder',
    'required',
    'readonly',
    'disabled',
    'invalid'
];

class TextField extends React.Component {
    render() {
        let attrs = {};
        $.extend(attrs,  this.props.attrs);
        for (let attr of textFieldProps) {
            let value = this.props[attr];
            if (value) {
                attrs[attr] = value;
            }

        }
        return (
            <input is="coral-textfield" ref={(elem) => this.element = elem}/>
        );
    }
    componentDidMount() {
        if (this.props.onChange) {
            $(this.element).change(function (e) {
                this.props.onChange(this.element.value);
            }.bind(this));
        }
    }
}

var selectProps = [
    'name',
    'placeholder',
    'multiple',
    'required',
    'readonly',
    'disabled',
    'invalid'
];

class Select extends React.Component {
    render() {
        let attrs = {};
        $.extend(attrs,  this.props.attrs);
        for (let attr of selectProps) {
            let value = this.props[attr];
            if (value) {
                attrs[attr] = value;
            }

        }
        return (
            <coral-select {...attrs} ref={(elem) => this.element = elem}>
                {this.props.children}
            </coral-select>
        );
    }
    componentDidMount() {
        $(this.element).on('change', function (e) {
            if (this.props.onChange) {
                this.props.onChange(this, this.element.value);
            }
        }.bind(this));
    }
    componentDidUpdate(prevProps, prevState) {
        this.element.value = this.props.value;
    }
}

class SelectItem extends React.Component {
    render() {
        let attrs = {};
        let text = this.props.text;
        if (!text && this.props.children && this.props.children && typeof this.props.children === 'string') {
            text = this.props.children;
        }
        return (
            <coral-select-item value={this.props.value} {...attrs} ref={(elem) => this.element = elem}>{text}</coral-select-item>
        );
    }
    componentDidMount() {
        if (this.props.selected) {
            this.element.selected = true;
        }
    }
}

class SelectList extends React.Component {
    render() {
        let attrs = {};
        $.extend(attrs,  this.props.attrs);
        for (let attr of selectProps) {
            let value = this.props[attr];
            if (value) {
                attrs[attr] = value;
            }

        }
        return (
            <coral-selectlist {...attrs} ref={(elem) => this.element = elem}>
                {this.props.children}
            </coral-selectlist>
        );
    }
    componentDidMount() {
        this.element.addEventListener('coral-selectlist:change', function (e) {
            if (this.props.onChange && this.element && this.element.selectedItem) {
                this.props.onChange(this, this.element.selectedItem.value);
            }
        }.bind(this));
    }
    componentDidUpdate(prevProps, prevState) {
        var value = this.props.value;
        $.each(this.element.selectedItems, function (index, item) {
            if (item.value === value) {
                item.selected = true;
            }
        })
    }
}

class SelectListItem extends React.Component {
    render() {
        let attrs = {};
        if (this.props.selected) {
            attrs['selected'] = true;
        }
        let text = this.props.text;
        if (!text && this.props.children && this.props.children && typeof this.props.children === 'string') {
            text = this.props.children;
        }
        return (
            <coral-selectlist-item value={this.props.value} {...attrs}>{text}</coral-selectlist-item>
        );
    }
}

var tagProps = [
    "closable",
    "multiline",
    "quiet",
    "id"
];

var iconProps = [
    "icon",
    "size"
];

class Tag extends React.Component {
    render() {
        let attrs = {};
        $.extend(attrs,  this.props.attrs);
        for (let attr of tagProps) {
            let value = this.props[attr];
            if (value) {
                attrs[attr] = value;
            }
        }
        let iconAttrs = {};
        for (let attr of iconProps) {
            let value = this.props[attr];
            if (value) {
                iconAttrs[attr] = value;
            }
        }
        return (
            <coral-tag {...attrs} value={this.props.value} ref={(elem) => this.element = elem}>
                <coral-icon {...iconAttrs}/>
                <coral-tag-label>
                    {this.props.text}
                </coral-tag-label>
            </coral-tag>
        );
    }
    componentDidMount() {
        var value = this.props.value;
        var onClose = this.props.onClose;
        var onClick = this.props.onClick;
        if (onClose) {
            $(this.element.firstChild).on('click', function () {
                onClose(value);
                return false;
            });
        }
        if (onClick) {
            $(this.element).on('click', function () {
                onClick(value);
                return false;
            });
        }
    }
}

class Table extends React.Component {
    render() {
        let properties = this.props.properties;
        let rowData = this.props.rows;
        let columns = [];
        let headers = [];
        let rows = [];
        $.each(properties, function (index, property) {
            columns.push(<col key={property} is="coral-col" sortable/>);
            headers.push(<th key={property} is="coral-th">{property}</th>);
        });
        $.each(rowData, function (index, row) {
            let rowColumns = [];
            $.each(properties, function (index, property) {
                rowColumns.push(<td key={property} is="coral-td">{row[property]}</td>);
            });
            rows.push(
                <tr key={index} is="coral-tr">
                    {rowColumns}
                </tr>
            );
        });

        return (
            <coral-table>
                <table is="coral-table-inner">
                    <colgroup>
                        {columns}
                    </colgroup>
                    <thead is="coral-thead">
                    <tr is="coral-tr">
                        {headers}
                    </tr>
                    </thead>
                    <tbody is="coral-tbody">
                    {rows}
                    </tbody>
                </table>
            </coral-table>
        )
    }
}

var Coral = window.Coral;

module.exports = {
    Button,
    TextField,
    Select,
    SelectItem,
    SelectList,
    SelectListItem,
    Tag,
    Table,
    Coral
};