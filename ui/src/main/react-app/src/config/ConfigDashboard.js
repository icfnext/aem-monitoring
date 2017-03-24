import React from 'react';
import $ from 'jquery';

import './ConfigDashboard.css'
import {Button, FieldWrapper, TextField, SelectList, SelectListItem} from '../coral/Coral';

class ClientList extends React.Component {
    render() {
        let items = [];
        if (this.props.clientList) {
            $.each(this.props.clientList, function (index, client) {
                items.push(<SelectListItem key={index} text={client.name} value={index}/>);
            });
        }
        return items.length === 0
            ? (<div></div>)
            : (
                <div className="monitoring-client-list">
                    <div>Clients:</div>
                    <SelectList
                        value={this.props.selectedClient}
                        onChange={this.props.clientChanged}
                        id="client-selector"
                    >
                        {items}
                    </SelectList>
                </div>
            );
    }
}

class ClientEditor extends React.Component {
    constructor() {
        super();
    }
    componentWillMount() {
        this.setState ({
            name: this.props.name,
            host: this.props.host,
            port: this.props.port,
            user: this.props.user,
            password: this.props.password,
        });
    }
    setHost(e) {
        this.setState({
            host: e.target.value
        });
    }
    setPort(e) {
        this.setState({
            port: e.target.value
        });
    }
    setUser(e) {
        this.setState({
            user: e.target.value
        });
    }
    setPassword(e) {
        this.setState({
            password: e.target.value
        });
    }
    render() {
        return (
            <div className="monitoring-client-form" ref={(elem) => this.element = elem} key={this.props.name}>
                <form className="coral-Form coral-Form--aligned u-columnLarge">
                    <section className="coral-Form-fieldset">
                        <FieldWrapper label="Host">
                            <TextField name="host" className="coral-Form-field" value={this.state.host} onChange={this.setHost.bind(this)}/>
                        </FieldWrapper>
                        <FieldWrapper label="Port">
                            <TextField name="port" className="coral-Form-field" value={this.state.port} onChange={this.setPort.bind(this)}/>
                        </FieldWrapper>
                        <FieldWrapper label="User">
                            <TextField name="user" className="coral-Form-field" value={this.state.user} onChange={this.setUser.bind(this)}/>
                        </FieldWrapper>
                        <FieldWrapper label="Password">
                            <TextField name="password" type="password" className="coral-Form-field" value={this.state.password} onChange={this.setPassword.bind(this)}/>
                        </FieldWrapper>
                    </section>
                    <input type="hidden" name="name" value={this.state.name}/>
                </form>
                <Button text="Delete" icon="delete" variant="warning" onClick={this.deleteClient.bind(this)}/>
                <div className="monitoring-client-update">
                    <Button text="Update" icon="save" onClick={this.submit.bind(this)}/>
                </div>
            </div>
        )
    }
    deleteClient() {
        if (this.props.name && this.props.name !== "") {
            $.ajax({
                type: 'POST',
                url: '/bin/monitoring/clients.json',
                data: {
                    delete: 'true',
                    name: this.state.name
                },
                success: this.update.bind(this)
            });
        } else {
            this.props.reset();
        }
    }
    submit() {
        $.ajax({
            type: 'POST',
            url: '/bin/monitoring/clients.json',
            data: {
                name: this.state.name,
                host: this.state.host,
                port: this.state.port,
                user: this.state.user,
                password: this.state.password,
            },
            success: this.update.bind(this)
        });
    }
    update() {
        this.props.update();
        this.props.reset();
    }
}

class ConfigDashboard extends React.Component {
    constructor() {
        super();
        this.state = {
            selectedClient: null
        };
    }
    render() {
        //let update = this.props.update;
        let clients = [];
        if (this.state.selectedClient) {
            let client = this.state.selectedClient;
            clients.push(
                <ClientEditor key={client.name} update={this.props.update} reset={this.reset.bind(this)} {...client}/>
            );
        }
        return (
            <div>
                <div id="wrapper">
                    <div id="nav">
                        <ClientList
                            clientList={this.props.clients}
                            selectedClient={this.state.selectedClient}
                            clientChanged={this.clientChanged.bind(this)}/>
                        <Button
                            text="Add Client"
                            onClick={this.newClient.bind(this)}
                            icon="add"
                            iconSize="XS"
                            block/>
                    </div>
                    <div id="content">
                        <div>&nbsp;</div>
                        {clients}
                    </div>
                </div>
            </div>
        );
    }
    clientChanged(source, clientIndex) {
        let client = this.props.clients[clientIndex];
        this.setState({
            selectedClient: client
        });
    }
    newClient() {
        this.setState({
            selectedClient: {name: ""}
        });
    }
    reset() {
        this.setState({
            selectedClient: null
        });
    }
}

export default ConfigDashboard;