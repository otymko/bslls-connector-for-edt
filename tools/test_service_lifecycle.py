"""Run LSService regressions with real child processes and isolated EDT/RPC peers."""
import argparse
import os
from pathlib import Path
import subprocess
import tempfile
import zipfile


def main():
    root = Path(__file__).resolve().parent.parent
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--java-home', type=Path, required=True, help='Matching JDK 17+ compiler and JVM')
    parser.add_argument('--source', type=Path, help='Alternative LSService.java for a negative control')
    args = parser.parse_args()
    source = args.source or root / ('bundles/com.github.otymko.dt.bsl.lspconnector/src/'
                                    'com/github/otymko/dt/bsl/lsconnector/service/LSService.java')
    suffix = '.exe' if os.name == 'nt' else ''
    java = args.java_home / 'bin' / ('java' + suffix)
    javac = args.java_home / 'bin' / ('javac' + suffix)
    fixtures = sorted((root / 'tools/lifecycle-test').rglob('*.java'))

    with tempfile.TemporaryDirectory(prefix='bslls-lifecycle-') as temporary:
        directory = Path(temporary)
        classes = directory / 'classes'
        classes.mkdir()
        subprocess.run([str(javac), '-encoding', 'UTF-8', '-proc:none', '--release', '17',
                        '-d', str(classes), str(source), *map(str, fixtures)], check=True)
        child_jar = directory / 'child.jar'
        with zipfile.ZipFile(child_jar, 'w') as archive:
            archive.writestr('META-INF/MANIFEST.MF',
                             'Manifest-Version: 1.0\r\nMain-Class: Sleeper\r\n\r\n')
            archive.write(classes / 'Sleeper.class', 'Sleeper.class')
        result = subprocess.run([str(java), '-Xmx64m', '-Dexternal=true', '-Djava=' + str(java),
                                 '-Djar=' + str(child_jar), '-classpath', str(classes),
                                 'LifecycleTest'], timeout=100)
        raise SystemExit(result.returncode)


if __name__ == '__main__':
    main()
