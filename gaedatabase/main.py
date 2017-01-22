# Copyright 2016 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

import webapp2
from google.appengine.ext import ndb

class Stepdata(ndb.Model):
    steps = ndb.IntegerProperty()
    too_long = ndb.BooleanProperty()
    reset = ndb.IntegerProperty()


class MainPage(webapp2.RequestHandler):
    def get(self):
        steps = self.request.get("steps")
        too_long = self.request.get("too_long")
        resp="NO WRITE"
        if steps and too_long:
            stepdata = Stepdata.get_by_id(1)
            originalreset = stepdata.reset
            
            if originalreset:
                stepdata.reset = 0
                stepdata.steps = 0
                stepdata.too_long = False
            else:
                stepdata.steps = int(steps)
                stepdata.too_long = bool(too_long=="1")
            stepdata.put()
            
            resp = str(originalreset)
            #resp = "steps:{}\ntoo_long:{}".format(stepdata.steps, stepdata.too_long)
        
        self.response.headers['Content-Type'] = 'text/plain'
        self.response.write(resp)

class InitPage(webapp2.RequestHandler):
    def get(self):
        Stepdata(id=1, steps=0, too_long=False, reset=1).put()
        
        self.response.headers['Content-Type'] = 'text/plain'
        self.response.write('Init done')

class ReadPage(webapp2.RequestHandler):
    def get(self):
        stepdata = Stepdata.get_by_id(1)
        resp = "{}\n{}".format(stepdata.steps, stepdata.too_long)
        self.response.headers['Content-Type'] = 'text/plain'
        self.response.write(resp)


app = webapp2.WSGIApplication([
    ('/', MainPage),
    ('/init', InitPage),
    ('/read', ReadPage),
], debug=True)
