# minecraft server auto closer
[![a12 maintenance: Slowly](https://api.anatawa12.com/short/a12-slowly-svg)](https://api.anatawa12.com/short/a12-slowly-doc)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/anatawa12/minecraft-server-auto-closer)](https://github.com/anatawa12/minecraft-server-auto-closer/releases/latest)

A mod to automatically close a server after
server started event. This is made for CI testing.

## How to this close server?

This closes server with ``stop command``.

## Stop delay configuration

This mod has a configuration to stop server after 
some seconds or ticks after server started event.

you can configure this by several ways.

### 1st way: From file in config directory

This mod finds `minecraft-server-auto-closer.txt`
in `config` folder. The content must be `{number} seconds`
, `{number} ticks`, or `beofre {token}`. When parse error, the file
will be ignored.
If ``before {token}`` is found, the server will be stopped
before something based on `{token}`. Current supported `{token}`s are shown below:

| `{token}` | when                      |
| :-------: |:--------------------------|
| `world`   | Stop before world loading | 

If found `{number} {unit}`, this mod waits specified ticks or seconds
before sends `stop` command.

### 2nd way: From file in mods directory

This mod finds `minecraft-server-auto-closer.txt`
and `{name of this mod jar}.txt` in `mods` folder.
The format of those files are same as the first way.

### 3rd way: From file name

This mod finds `stop-after-{number}-seconds` or
`stop-after-{number}-ticks` in name of jar file.
If found, this mod waits specified ticks or seconds
before sends `stop` command.
For `minecraft-server-auto-closer-1.0-stop-after-20-ticks.jar`,
this mod waits 20 ticks after server start event.

## License

This software is published under MIT License.
See [LICENSE](LICENSE) for more information.
