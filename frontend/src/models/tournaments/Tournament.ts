import Topic from '@/models/management/Topic';

export class Tournament {
  id: number | null = null;
  creatorID: number | null = null;
  creatorName: string | null = null;
  numQuests: number | null = null;
  quizID: number | null = null;
  topics: Topic[] = [];
  enrolledStudentsIds!: number[];
  startTime!: number[];
  endTime!: number[];
  startTimeString!: string;
  endTimeString!: string;
  status!: string;

  constructor(jsonObj?: Tournament) {
    if (jsonObj) {
      this.id = jsonObj.id;
      this.creatorID = jsonObj.creatorID;
      this.creatorName = jsonObj.creatorName;
      this.numQuests = jsonObj.numQuests;
      this.quizID = jsonObj.quizID;
      this.enrolledStudentsIds = jsonObj.enrolledStudentsIds;
      this.topics = jsonObj.topics;
      this.startTime = jsonObj.startTime;
      this.endTime = jsonObj.endTime;
      this.getDateFormatted(jsonObj);
      this.status = jsonObj.status;
    }
  }

  getDateFormatted(t: Tournament):void{
    let start = String(t.startTime).split(',');
    let end = String(t.endTime).split(',');
    this.startTimeString = start[0] + '-' + start[1] + '-' + start[2] + ' ' + start[3]+ ':'+start[4];
    this.endTimeString = end[0] + '-' + end[1] + '-' + end[2] + ' ' + end[3]+ ':'+end[4];
  }

}