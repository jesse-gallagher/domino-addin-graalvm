# Domino Server Addin GraalVM Example

This example shows the use of GraalVM's Native Image capability to build a Domino server addin in Java, using components of Notes.jar to perform addin tasks

## Building and Running

To run this example:

- Copy Notes.jar into the project root directory
- Copy a functional server.id, names.nsf, and notes.ini into the "docker/notesdata" directory
- Install Docker
- Load the [Domino on Docker](https://help.hcltechsw.com/domino/11.0.1/admin/inst_dock_domino_overview.html) 11.0.1 image, available from Flexnet.
- Run `./run.sh` or manually build and run the Docker image from this directory

When Domino launches, you should see a log line saying "GraalVM Test initialized". You can have the task echo back text by running e.g. `tell graalvm-test hello`.

## Limitations

The example is severely limited, even beyond the fact that all it does is echo back what you tell it. In particular, it would take additional work to get normal Notes.jar operations working, since the GraalVM compiler isn't yet configured to include all the required metadata to make that function.