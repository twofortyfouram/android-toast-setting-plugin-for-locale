# Home Assistant Plug-In for Tasker
This is a Tasker plug-in to allow calling services on and getting entity states from a Home Assistant installation.

# Available on Google Play
https://play.google.com/store/apps/details?id=com.markadamson.taskerplugin.homeassistant

## Requirements
- Android 4.0 (API 14)
- [Tasker](https://tasker.joaoapps.com/)
- [Home Assistant 0.78](https://www.home-assistant.io/)

## Usage

### Add A Server
- Generate a [Long-Lived Access Token](https://www.home-assistant.io/docs/authentication/) in Home Assistant.
- Create a new Task in Tasker.
- Add an Action: 'Plugin' > 'Home Assistant Plug-In for Tasker' to the Task.
- Tap the edit button by 'Configuration'.
- Tap the '+' button near the top right to add a new Home Assistant server.
- Enter the details for your Home Assistant server. The Base URL must include the protocol, and **not** a trailing backslash (eg: `https://my.home-assistant.com`).
- Test the server, then click 'Save'.

### Call A Service
- Create a new Task in Tasker.
- Add the Action 'Plugin' > 'Home Assistant Plug-In for Tasker' > 'Call Service' to the Task.
- Select a Server, or add a new one as above.
- Select a Service, and optionally enter Service Data in JSON format.
- Test the Service call, then click 'Save'.

### Get An Entity's State
- Create a new Task in Tasker.
- Add the Action 'Plugin' > 'Home Assistant Plug-In for Tasker' > 'Get State' to the Task.
- Select a Server, or add a new one as above.
- Select an Entity.
- Enter a Tasker variable name, including the leading `%`.
- Click 'Save'.
- **Make sure to set a timeout - this tells the action to await a result, otherwise the task continues instantly and the variable will not be populated!**

### Variables
Variables are supported in the following fields:
- 'Call Service' > 'Service'
- 'Call Service' > 'Service Data'
- 'Get State' > 'Entity ID'

Remember to always include the leading `%`

## To Do
- [x] Actions
- [x] Tasker Variables
