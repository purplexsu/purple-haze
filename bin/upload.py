"""Main Program of monitoring."""

__author__="purplexsu"

import base64
import ftplib
import io
import os
import os.path
import re
import sys
import urllib2
import zipfile

BASE_DIR = os.environ['HOME'] + '/My Webs/PurpleHaze'

VALID_DIVISIONS = (
'appraisal',
'blog',
'movie',
'music',
'photo',
'travel'
)

COMMENT_PATTERN = re.compile(r'^comment-\d{3}\.html$')
COLUMN_PATTERN = re.compile(r'^column-[a-z0-9]*-\d{2}\.html$')
PHOTO_PATTERN = re.compile(r'^\d{2}\.html$')
PHOTO_ALBUM_PATTERN = re.compile(r'^\d{3}$')
ARTICLE_PATTERN = re.compile(r'^article-\d{3}-\d{2}\.html$')
INDEX_PATTERN = re.compile(r'^index.*\.html$')

def IsValidDivision(dir):
  return dir in VALID_DIVISIONS
  
def IsPhotoAlbum(dir):
  return PHOTO_ALBUM_PATTERN.match(dir)

def IsComment(filename):
  return COMMENT_PATTERN.match(filename)

def IsColumn(filename):
  return COLUMN_PATTERN.match(filename)

def IsPhoto(filename):
  return PHOTO_PATTERN.match(filename) or 'index.html' == filename

def IsArticle(filename):
  return ARTICLE_PATTERN.match(filename)

def IsIndex(filename):
  return INDEX_PATTERN.match(filename)
  
def AddToZipFile(arg, dir, files):
  dirfilter = arg[0]
  filefilter = arg[1]
  zip = arg[2]
  additionalArcPath = ''
  if len(arg) == 4:
    additionalArcPath = arg[3]
  dirname = os.path.basename(dir)
  if not dirfilter(dirname):
    return
  arcPath = additionalArcPath + dirname + '/'
  for file in files:
    if filefilter(file):
      zip.write(dir + '/' + file, arcPath + file)

def ZipArticle(zip):
  os.path.walk(BASE_DIR, AddToZipFile, 
              (IsValidDivision, IsArticle, zip))
  zip.write(BASE_DIR + '/' + 'comments.html', 'comments.html')

def ZipPhoto(zip):
  os.path.walk(BASE_DIR, AddToZipFile, 
              (IsPhotoAlbum, IsPhoto, zip, 'photo/'))

def ZipComment(zip):
  os.path.walk(BASE_DIR, AddToZipFile, 
              (IsValidDivision, IsComment, zip))

def ZipColumn(zip):
  os.path.walk(BASE_DIR, AddToZipFile, 
              (IsValidDivision, IsColumn, zip))

def ZipIndex(zip):
  os.path.walk(BASE_DIR, AddToZipFile, 
              (IsValidDivision, IsIndex, zip))
  zip.write(BASE_DIR + '/' + 'index.html', 'index.html')
  zip.write(BASE_DIR + '/' + 'friends.html', 'friends.html')
  
def Upload(targetfile, credential):  
  ftp = ftplib.FTP('ftp.purplexsu.net',
		   credential.get('default', 'ftp.username'),
		   credential.get('default', 'ftp.password'))
  ftp.cwd('www/cmdcmd')
  ftp.storbinary('stor update.zip', open(targetfile, 'rb'))
  ftp.close()
  
def Extract(credential):
  request = urllib2.Request('http://www.purplexsu.net/cmdcmd/cmdcmd.php?cmdcmd=2')
  user = config.get('default', 'http.username')
  password = config.get('default', 'http.password')
  authorization = base64.standard_b64encode('%s:%s' % (user, password))
  request.add_header('Authorization', 'Basic %s' % authorization)
  result = urllib2.urlopen(request)
  print result.read()

def ReadCredential():
  cFile = open(os.environ['HOME'] + '/.purple-haze', 'r')
  # fake ConfigParser format, add a default section header:
  tmp = '[default]\n' + a.read()
  cFile.close()
  credential = ConfigParser.ConfigParser();
  credential.readfp(io.BytesIO(tmp))
  return credential;

def main():
  credential = ReadCredential()
  print 'which type of files:[a]rticle,[c]olumn,[i]ndex,c[o]mment,[p]hoto:'
  input = sys.stdin.readline().rstrip().split(',')
  targetfile = BASE_DIR + '/update.zip'
  zip = zipfile.ZipFile(targetfile, 'w', zipfile.ZIP_DEFLATED)
  if 'a' in input:
    ZipArticle(zip)
  if 'c' in input:
    ZipColumn(zip)
  if 'i' in input:
    ZipIndex(zip)
  if 'o' in input:
    ZipComment(zip)
  if 'p' in input:
    ZipPhoto(zip)
  zip.close()
  Upload(targetfile, credential)
  Extract(credential)
  raw_input("press <ENTER> to quit\n")
  #end main()

if __name__=='__main__':
  main()
  #end
