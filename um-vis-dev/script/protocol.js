/**
 * This document describes the protocol for retrieving the current educational state of a learner.  The
 * protocol will evolve over time to accomodate the requirements of the project.
 */


// --------------------------------------------------------------------------------------------------------
// MAIN object (i.e., the response of the server)
// --------------------------------------------------------------------------------------------------------

{
  version: "0.0.3",
  
  context: {
    learnerId : "LEARNER.id",  // ID of the current learner (i.e., the one requesting the data)
    group     : {
      id         : "IS172013Spring",
      name       : "Java Programming, Spring 2013",
      learnerCnt : 17
    }
  },
  
  reportLevels: [
    REPORT_LEVEL,
    ...
  ],
  
  resources: [
    RESOURCE,
    ...
  ],
  
  topics: [
    TOPIC,
    ...
  ],
  
  learners: [
    LEARNER,
    ...
  ],
  
  groups: [  // aggregates over groups of learners
    GROUP,
    ...
  ],
  
  timeline: {
    name: "Week"
  },
  
  feedback: {  // [not needed yet]
    rate: 0.25  // rate of collecting feedback (i.e., after how many activities to ask a question) [not needed yet]
  },
  
  vis: {     // visualization-specific data
    color         : {
      binCount    : 7,  // [optional]
      value2color : function (x) { var y = Math.log(x)*0.25 + 1;  return (y < 0 ? 0 : y); }  // [optional]
    },
    topicSizeAttr : ["difficulty", "importance"],
    ui: {
//      doShowToolbar            : true,
//      doShowToolbarReportLevel : true,
//      doShowToolbarMode        : true,
//      doShowToolbarTopicSize   : true,
//      doShowToolbarGroup       : true,
//      doShowToolbarResource    : true,
//      
//      doShowOtherLearners : true
        params : {user :{}, group: {}}
        
    }
  }
}



// --------------------------------------------------------------------------------------------------------
// REPORT_LEVEL object
// --------------------------------------------------------------------------------------------------------

{ id: "p", name: "Progress",  isDefault: true  }
{ id: "k", name: "Knowledge", isDefault: false }



// --------------------------------------------------------------------------------------------------------
// RESOURCE object
// --------------------------------------------------------------------------------------------------------

{
  id            : "ex",
  name          : "Examples",
  updateStateOn : {
    done          : true,   // when activities have a culmination event and call 'parent.vis.actDone(res)' (e.g., QuizJet)
    winClose      : false,  // when activities cannot be modified and we don't know what happens inside of the activity frame
    winCloseIfAct : false   // when activities don't have a culmination event and therefore do not call 'parent.vis.actDone(res)' but report the user's activity (hence 'IfAct' in the name) by calling 'parent.vis.actUpdState(isImmediate)' (e.g., WebEx); note that 'parent.vis.actUpdState(isImmediate)' can be used to force an immediate state update and in that circumstance the 'winCloseIfAct' property should be set to false
  },
  isDefault     : true,
  
  feedbackForm  : [  // [not needed yet]
    FEEDBACK_FORM_ITEM,
    ...
  ],
  url: "http://www.example.com/collect-feedback"
}



// --------------------------------------------------------------------------------------------------------
// FEEDBACK_FORM_ITEM object
// --------------------------------------------------------------------------------------------------------

{
  id       : "feedback-01",
  text     : "How difficult was this example?",
  response : [  // if 'null' provide an open-question text area
    { value: 0, label: "Easy"   },
    { value: 1, label: "Medium" },
    { value: 2, label: "Hard"   }
  ]
}



// --------------------------------------------------------------------------------------------------------
// TOPIC object
// --------------------------------------------------------------------------------------------------------

{
  id         : "variable-declaration",
  name       : "Variable declaration",
  difficulty : 0.4,
  importance : 0.8,
  timeline   : { name: "", covered: false, current: true },
  
  concepts   : [  // [not needed yet]
    CONCEPT,
    ...
  ],
  
  activities : {
    "RESOURCE.id": [
      ACTIVITY,
      ...
    ],
    ...
  }
}



// --------------------------------------------------------------------------------------------------------
// ACTIVITY object
// --------------------------------------------------------------------------------------------------------

{
  id           : "activity-01",
  name         : "Quiz on variables",
  url          : "http://example.org/activity-01.html"
  accessCnt    : 4,    // the number of time the activity has been accessed by the learner [not needed yet]
  completeness : 0.9,  // for examples, readings, etc. [not needed yet]
  successRate  : 0.6,  // for quizes, etc. [not needed yet]
  
  allowManualRecRetrieval : true,  // [optional, i.e., defaults to false]
  
  concepts     : [     // [not needed yet]
    CONCEPT,
    ...
  ]
}



// --------------------------------------------------------------------------------------------------------
// CONCEPT object
// --------------------------------------------------------------------------------------------------------

// [not needed yet]

{
  name   : "pre-increment",
  weight : 0.3
}



// --------------------------------------------------------------------------------------------------------
// LEARNER object
// --------------------------------------------------------------------------------------------------------

{
  id    : "tol7",          // for privacy reasons, this may be obfuscated for other learners
  name  : "Tomek Loboda",  // for privacy reasons, this may be set empty for other learners
  state : STATE
}



// --------------------------------------------------------------------------------------------------------
// GROUP object
// --------------------------------------------------------------------------------------------------------

// There can be many GROUP objects so that the learner (or the client application) can choose what they 
// want (or should) be compared against.  Examples include "class average," "10 fastest learners," "10 
// average learners," "myself right before midterm," or "myself a month ago."

{
  name       : "Class average",
  state      : STATE,  // in the case of this example aggregation, state is the average for the entire class
  learnerIds : [       // IDs of learners that participate in the aggregation
    "LEARNER.id",
    ...
  ],
  isDefault  : true
}


// --------------------------------------------------------------------------------------------------------
// STATE object
// --------------------------------------------------------------------------------------------------------

{
  topics: {
    "TOPIC.id": {
      values: {
        "RESOURCE.id": {
          "REPORT_LEVEL.id": 0.1,
          ...
        },
        ...
      },
      sequencing: {
        "RESOURCE.id": 0.4,
        ...
      }
    },
    ...
  },
  
  activities: {
    "TOPIC.id": {
      "RESOURCE.id": {
        "ACTIVITY.id": {
          values: {
            "REPORT_LEVEL.id": 0.1,
            ...
          },
          sequencing: 0.3
        },
        ...
      },
      ...
    },
    ...
  }
}


// Example:
{
  topics: {
    "class"       : { values: { "ex": { "p": 0.1, "k": 0.0 }, "quiz": { "p": 0.4, "k": 0.0 }, "read": { "p": 0.4, "k": 0.0 } }, sequencing: 0.1 },
    "object"      : { values: { "ex": { "p": 0.4, "k": 0.0 }, "quiz": { "p": 0.6, "k": 0.0 }, "read": { "p": 0.6, "k": 0.0 } }, sequencing: 0.8 },
    "inheritance" : { values: { "ex": { "p": 0.2, "k": 0.0 }, "quiz": { "p": 0.7, "k": 0.0 }, "read": { "p": 1.0, "k": 0.0 } }, sequencing: 0.0 }
  },
  activities: {
    "class": {
      "quiz": [
        "quiz01": { values: { "k": 0.0, "p": 1.0 }, sequencing: 0.0 },
        "quiz02": { values: { "k": 0.2, "p": 0.8 }, sequencing: 0.0 },
        "quiz03": { values: { "k": 0.4, "p": 0.6 }, sequencing: 0.0 },
      ]
    }
  }
}

