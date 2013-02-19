"""A lib to load and parse credential data."""

__author__="purplexsu"

import ConfigParser
import io
import os

def ReadCredential():
  cFile = open(os.environ['HOME'] + '/.purple-haze', 'r')
  # fake ConfigParser format, add a default section header:
  tmp = '[default]\n' + cFile.read()
  cFile.close()
  credential = ConfigParser.ConfigParser();
  credential.readfp(io.BytesIO(tmp))
  return credential;
