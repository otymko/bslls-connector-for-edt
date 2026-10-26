"""Run the connector startup regression with an installed EDT and LSP4J target."""
import argparse
import os
from pathlib import Path
import subprocess
import tempfile

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('--classpath', required=True, help='Installed connector, EDT plugins and target LSP4J jars')
parser.add_argument('--classes-out', type=Path, help='Retain compiled classes for inspecting the test')
parser.add_argument('--java-home', type=Path, required=True, help='JDK 17 or newer; compiler and JVM must match')
args = parser.parse_args()
root = Path(__file__).resolve().parent.parent
source = root / 'bundles/com.github.otymko.dt.bsl.lspconnector/src/com/github/otymko/dt/bsl/lsconnector/lsp/BSLConnector.java'
with tempfile.TemporaryDirectory() as temporary:
    classes = args.classes_out or Path(temporary)
    classes.mkdir(parents=True, exist_ok=True)
    fixtures = list((root / 'tools/startup-test').rglob('*.java'))
    suffix = '.exe' if os.name == 'nt' else ''
    subprocess.run([str(args.java_home / 'bin' / ('javac' + suffix)), '-encoding', 'UTF-8', '-proc:none', '-classpath', args.classpath,
        '-d', str(classes), str(source), *map(str, fixtures)], check=True)
    subprocess.run([str(args.java_home / 'bin' / ('java' + suffix)), '-classpath', str(classes) + os.pathsep + args.classpath,
        'ConnectorStartupTest'], check=True)
