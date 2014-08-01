// http://adapt2.sis.pitt.edu/guanjie/GetContentLevels?usr=peterb&grp=IS172012Fall&mod=user&sid=TEST001&lastActivityId=...&result=0

{
  lastActivityId: "",
  
  learner: { ... },
  
  feedback: {
    id: 54,
    items: [
      {
        text     : "How difficult was this example?",
        type     : one, many, text
        required : true,
        response : [
          { value: 0, label: "Easy"   },
          { value: 1, label: "Medium" },
          { value: 2, label: "Hard"   }
        ]
      }
    ]
  },
  
  recommendationName: "Recommended examples",
  recommendation: [
    { recommendationId: 1, topicId: "", resourceId: "", activityId: "", feedback: { text: "" } },
    { recommendationId: 2, topicId: "", resourceId: "", activityId: ""                         },
    ...
  ]
}
