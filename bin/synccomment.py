"""Main Program of sync comment files from server to local."""

__author__="purplexsu"

import base64
import credential
import os
import tempfile
import urllib2
import zipfile

BASE_DIR = os.environ['HOME'] + '/My Webs/PurpleHaze/'

TMP_ZIP_FILE = tempfile.gettempdir() + '/comment.zip'

DOWNLOAD_CACHE_SIZE = 8192

def Download():
  c = credential.ReadCredential()
  request = urllib2.Request('http://www.purplexsu.net/cmdcmd/cmdcmd.php?cmdcmd=1')
  user = c.get('default', 'http.username')
  password = c.get('default', 'http.password')
  authorization = base64.standard_b64encode('%s:%s' % (user, password))
  request.add_header('Authorization', 'Basic %s' % authorization)
  result = urllib2.urlopen(request)
  meta = result.info()
  file_size = int(meta.getheaders("Content-Length")[0])
  print "Downloading: %s bytes..." % file_size
  output = open(TMP_ZIP_FILE, 'wb')

  downloaded_bytes = 0
  while True:
    buf = result.read(DOWNLOAD_CACHE_SIZE)
    if not buf:
      break
    downloaded_bytes += len(buf)
    output.write(buf)
    status = r"%10d  [%3.2f%%]" % (downloaded_bytes,
				   downloaded_bytes * 100.0 / file_size)
    status = status + chr(8)*(len(status)+1)
    print status,
  output.close()
  #end Download

def Unzip():
  zip = zipfile.ZipFile(TMP_ZIP_FILE, 'r')
  for file in zip.namelist():
    print "Extracting %s" % file
    target = open(BASE_DIR + file, 'wb')
    target.write(zip.read(file))
    target.close()
  zip.close()
  #end Unzip

def main():
  Download()
  Unzip()
  raw_input("press <ENTER> to quit\n")
  #end main()

if __name__=='__main__':
  main()
  #end
