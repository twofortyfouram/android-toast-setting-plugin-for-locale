# Home Assistant Plug-In for Tasker
This is a Tasker plug-in to allow calling services on a Home Assistant installation.

## Requirements
- Android 4.0 (API 14)
- [Tasker](https://tasker.joaoapps.com/)
- [Home Assistant 0.78](https://www.home-assistant.io/)

## Usage
- Generate a [Long-Lived Access Token](https://www.home-assistant.io/docs/authentication/) in Home Assistant.
- Create a new Task in Tasker.
- Add the Action 'Plugin' > 'Home Assistant Plug-In for Tasker' to the Task.
- Tap the edit button by 'Configuration'.
- Tap the '+' button near the top right to add a new Home Assistant server.
- Enter the details for your Home Assistant server. The Base URL must include the protocol, and **not** a trailing backslash (eg: `https://my.home-assistant.com`).
- Test the server, then click 'Save'.
- Select a Service, and optionally enter Service Data in JSON format.
- Test the Service call, then click 'Save'.

## To Do
- [x] Actions
- [ ] Conditions
- [ ] Tasker Variables